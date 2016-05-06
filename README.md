# Hibernate Sequences WIP

Project showcasing Hibernate ID generation with sequences. It covers different ID generation strategies/optimizers and 
a sample custom sequence ID generator.

Hibernate is a very useful tool to map Java objects to relational database models in a simple manner. However this simplicity
forces Hibernate to do a lot of things behind the scene and assume a lot of default behaviours. Someone new to Hibernate 
might not expect certain assumptions it makes. One such field within Hibernate is ID generation strategies.

## Usage
* Make sure you have Postgres running and create a database for this project.
* Review the config to amend database connection details and logging level (`INFO` recommended for performance runs, `DEBUG` 
for inspecting Hibernate usage)
* Run the project with `./go`
* You can fiddle around by hitting the endpoints that create test entities and use the sequences as a consequence.
* You can observe Hibernate logs and metrics to see the time required to perform the transaction and to see when the sequences are being queried.

## Sample calls
```
curl localhost:9014/hibSeq/sequenceGen/123
curl localhost:9014/hibSeq/genericSeq/123
curl localhost:9014/hibSeq/genericSeqHiLo/123
curl localhost:9014/hibSeq/genericSeqPooledLo/123
curl localhost:9014/hibSeq/genericSeqPooled/123
```
The number at the end of the path represents how many entities should be created at a time.

Starting the performance run:
```
curl localhost:9014/hibSeq/perf/<num_entries>/<num_passes>
```
for example `curl localhost:9014/hibSeq/perf/200000/50` will start a performance check by inserting 200000 entities 
50 times (in order to calculate the average time) for all strategies.

## Automatically generated ID values with sequences by Hibernate - example with Dropwizard 0.9.2

### Introduction
Sequences allow for easy and flexible way of generating identifiers for database entities. Not all
databases support sequences, but when they do, sequences are oftentimes the go to strategy for generating object identifiers.

Hibernate is a very popular object mapping framework for Java. Thanks to its popularity there are a lot of
resources online and you get the assurance that it is tried and tested. However this should not stop us from
taking a deeper look into what we are actually using. Especially if we are setting up a project and we suspect
that our usage of Hibernate will get replicated across the project for consistency reasons by other developers.

We are going to explore different ways of generating ID values with sequences and consider their pros and cons. To demonstrate
different ID generation strategies I've set up a sample Dropwizard service that persists entities comprised of a single ID column.

In this example I'm using Dropwizard 0.9.2. This particular version of Dropwizard uses **Hibernate 4.3.11.Final** under the hood.

### Main sequence based generation strategies

#### Sequence with no optimizer 
Uses a sequence to determine the next ID value. The ID values generated are equal to the current sequence value.

###### Implementation (org.hibernate.id.SequenceGenerator)
```
public Serializable generate(SessionImplementor session, Object obj) {
    return generateHolder( session ).makeValue();
}

protected IntegralDataTypeHolder generateHolder(SessionImplementor session) {
    try {
        PreparedStatement st = session.getTransactionCoordinator().getJdbcCoordinator().getStatementPreparer().prepareStatement( sql );
        try {
            ResultSet rs = session.getTransactionCoordinator().getJdbcCoordinator().getResultSetReturn().extract( st );
            try {
                rs.next();
                IntegralDataTypeHolder result = buildHolder();
                result.initialize( rs, 1 );
                LOG.debugf( "Sequence identifier generated: %s", result );
                return result;
            }
            finally {
                session.getTransactionCoordinator().getJdbcCoordinator().release( rs, st );
            }
        }
        finally {
            session.getTransactionCoordinator().getJdbcCoordinator().release( st );
        }

    }
    catch (SQLException sqle) {
        throw session.getFactory().getSQLExceptionHelper().convert(
                sqle,
                "could not get next sequence value",
                sql
        );
    }
}
```

Note that this generation strategy is NOT `synchronized`. This prevents Hibernate ID generation 
from being too much of a bottleneck in multi-threaded batch inserts. The database itself ensures that the values fetched from 
a sequence are never duplicate and the implementation is thread-safe.

#### Sequence with HiLo optimizer
Uses the database sequence to store and retrieve the `"high" number` and uses 
it to generate new identifiers for maximum of `"low" number` entities. In this particular implementation 
the range of IDs that get assigned is given by the following formula: `( (high - 1) * low, high * low ]` where 
`low` changes within the range defined by the `increment_size` property and is `[1, increment_size]`. 
When new range is being started the sequence value in the database is being incremented by `1`.

###### Implementation (org.hibernate.id.enhanced.HiLoOptimizer)
```
public synchronized Serializable generate(AccessCallback callback) {
    final GenerationState generationState = locateGenerationState( callback.getTenantIdentifier() );

    if ( generationState.lastSourceValue == null ) {
        // first call, so initialize ourselves.  we need to read the database
        // value and set up the 'bucket' boundaries
        generationState.lastSourceValue = callback.getNextValue();
        while ( generationState.lastSourceValue.lt( 1 ) ) {
            generationState.lastSourceValue = callback.getNextValue();
        }
        // upperLimit defines the upper end of the bucket values
        generationState.upperLimit = generationState.lastSourceValue.copy().multiplyBy( incrementSize ).increment();
        // initialize value to the low end of the bucket
        generationState.value = generationState.upperLimit.copy().subtract( incrementSize );
    }
    else if ( ! generationState.upperLimit.gt( generationState.value ) ) {
        generationState.lastSourceValue = callback.getNextValue();
        generationState.upperLimit = generationState.lastSourceValue.copy().multiplyBy( incrementSize ).increment();
    }
    return generationState.value.makeValueThenIncrement();
}
```

Since this method of generating ID values forces the HiLoOptimizer to hold `high` and `low` values in memory the method 
is `synchronized`. Even though HiLoOptimizer saves a lot of database calls and the generate method is not compute intensive 
we can imagine a scenario where this could be a problem. If we try to insert a lot of entities using the same optimizer 
into a highly efficient clustered database we might actually degrade performance by using this optimization with certain 
`increment_size` settings. That being said this is an unlikely scenario - in most use cases reducing the amount of database 
calls increases performance.

#### Sequence with Pooled optimizer 
As is the case with HiLo this optimizer assigns ranges of values based on the `increment_size` parameter. 
However Pooled optimizer keeps the maximum value of the current range as the sequence value in the database. 
This means that the sequence value needs to be incremented by `increment_size` every time a new range is starting.
Current range that is being assigned is `(sequence_value - increment_size, sequence_value]`. 
One exception is the very first range assuming that we start from `1`. 
In such case Pooled optimizer assigns the entity being persisted with initial value and fetches a new range.

###### Implementation (org.hibernate.id.enhanced.PooledOptimizer)
```
public synchronized Serializable generate(AccessCallback callback) {
    final GenerationState generationState = locateGenerationState( callback.getTenantIdentifier() );

    if ( generationState.hiValue == null ) {
        generationState.value = callback.getNextValue();
        // unfortunately not really safe to normalize this
        // to 1 as an initial value like we do the others
        // because we would not be able to control this if
        // we are using a sequence...
        if ( generationState.value.lt( 1 ) ) {
            log.pooledOptimizerReportedInitialValue( generationState.value );
        }
        // the call to obtain next-value just gave us the initialValue
        if ( ( initialValue == -1
                && generationState.value.lt( incrementSize ) )
                || generationState.value.eq( initialValue ) ) {
            generationState.hiValue = callback.getNextValue();
        }
        else {
            generationState.hiValue = generationState.value;
            generationState.value = generationState.hiValue.copy().subtract( incrementSize - 1 );
        }
    }
    else if ( generationState.value.gt( generationState.hiValue ) ) {
        generationState.hiValue = callback.getNextValue();
        generationState.value = generationState.hiValue.copy().subtract( incrementSize - 1 );
    }

    return generationState.value.makeValueThenIncrement();
}
```

Similarly to HiLoOptimizer PooledOptimizer is synchronized, so it suffers the same drawbacks.

#### Sequence with PooledLo optimizer
This one operates similarly to Pooled optimizer, but instead of keeping the last value of the 
range that is being currently assigned, it keeps the range starting value. 
Range that is being assigned is `[sequence_value, sequence_value + increment_size)`.

###### Implementation (org.hibernate.id.enhanced.PooledLoOptimizer)
```
public synchronized Serializable generate(AccessCallback callback) {
    final GenerationState generationState = locateGenerationState( callback.getTenantIdentifier() );

    if ( generationState.lastSourceValue == null
            || ! generationState.value.lt( generationState.upperLimitValue ) ) {
        generationState.lastSourceValue = callback.getNextValue();
        generationState.upperLimitValue = generationState.lastSourceValue.copy().add( incrementSize );
        generationState.value = generationState.lastSourceValue.copy();
        // handle cases where initial-value is less that one (hsqldb for instance).
        while ( generationState.value.lt( 1 ) ) {
            generationState.value.increment();
        }
    }
    return generationState.value.makeValueThenIncrement();
}
```

Similarly to the two previous optimizers PooledLoOptimizer is synchronized.

### javax.persistence.SequenceGenerator annotation

You can often find that, in order to generate ID values from a sequence people use `javax.persistence.SequenceGenerator` annotation
instead of `org.hibernate.annotations.GenericGenerator`.
```
@SequenceGenerator(name = "gen_value_sequence", sequenceName = "gen_value_sequence")
```
This is fine if you know what this does, but if you do not, you might quickly encounter a lot of issues.

First thing worth noting is that if you do not specify `allocationSize` parameter the generation optimization strategy 
chosen by default in this particular version of Hibernate is `Pooled` and the `increment_size` is set to `50`. On older
versions of Hibernate `SequenceGenerator` defaulted to `HiLo`. You can still force newer versions of Hibernate to use `HiLo` if you set 
`hibernate.id.new_generator_mappings` Hibernate config property to `false`. This default behaviour might cause issues if 
you are creating the database with migrations and do not set the database sequence increment size to match what Hibernate expects.

However if you do set `SequenceGenerator` property `allocationSize` to `1` it will revert back to using the plain 
unoptimized sequence generation strategy.

### Custom ID generation strategies

Sometimes circumstances force us to generate ID values that are not covered by existing Hibernate generators. For example
we might be forced to create a mapping for a legacy database that cannot be changed, or simply the requirements force us to do so.
In such case custom ID generation strategies may come in handy. Instead of using existing Hibernate generators you can
implement your own ID generator. You can do that either by implementing one of Hibernate generator interfaces like the most basic
`org.hibernate.id.IdentifierGenerator` or other interfaces like `org.hibernate.id.PersistentIdentifierGenerator`. 
If all you need to do is apply some tweaks to an existing strategy you can also extend existing generators.

Let's consider a (very unlikely) scenario that we need to create a String ID that is a concatenation of values originating
from two database sequences. Let us assume that the format would be `ID1_ID2`. 

#### Implementation
Sample implementation can be seen [here](src/main/java/com/kainos/learn/hibseq/db/id/generator/DoubleSequenceGenerator.java).
Implementing the `org.hibernate.id.Configurable` interface allows us to parse parameters that are provided when 
creating a mapping for a DB entity.
```
public void configure(Type type, Properties params, Dialect dialect) throws MappingException {
    ObjectNameNormalizer normalizer = (ObjectNameNormalizer) params.get(IDENTIFIER_NORMALIZER);
    secondSequenceName =  normalizer.normalizeIdentifierQuoting(
            ConfigurationHelper.getString(SECOND_SEQ, params)
    );
    sequenceName = normalizer.normalizeIdentifierQuoting(
            ConfigurationHelper.getString(FIRST_SEQ, params, "hibernate_sequence")
    );

    sql = dialect.getSequenceNextValString(sequenceName);
    sql2 = dialect.getSequenceNextValString(secondSequenceName);
}
```
Additionally we can prepare sql statements that will be used to fetch new values from the DB sequences.

Its also worth noting that this generator implements `org.hibernate.id.PersistentIdentifierGenerator` interface. This means
that we additionally provide methods allowing Hibernate to create and delete all the necessary DB objects 
automatically when `hibernate.hbm2ddl.auto` config is in use. In this case we create or delete two sequences 
needed to generate the ID.
```
@Override
public String[] sqlCreateStrings(Dialect dialect) throws HibernateException {
    String[] ddl = Stream.concat(
            Arrays.stream(dialect.getCreateSequenceStrings(sequenceName, 1, 1)),
            Arrays.stream(dialect.getCreateSequenceStrings(secondSequenceName, 1, 1)))
            .toArray(String[]::new);

    return ddl;
}

@Override
public String[] sqlDropStrings(Dialect dialect) throws HibernateException {
    String[] ddl = Stream.concat(
            Arrays.stream(dialect.getDropSequenceStrings(sequenceName)),
            Arrays.stream(dialect.getDropSequenceStrings(secondSequenceName)))
            .toArray(String[]::new);

    return ddl;
}
```
The generation method is similar to that of regular `SequenceGenerator` however we query two DB sequences and concatenate
the results.
```
public Serializable generate(final SessionImplementor session, Object obj) {
    Long value = null;
    while (value == null || value < 0) {
        value = generateValue(session, sql);
    }

    Long value2 = null;
    while (value2 == null || value2 < 0) {
        value2 = generateValue(session, sql2);
    }

    return value + "_" + value2;
}

private Long generateValue(SessionImplementor session, String query) {
    try {
        PreparedStatement st = session.getTransactionCoordinator().getJdbcCoordinator().getStatementPreparer().prepareStatement(query);
        try {
            ResultSet rs = session.getTransactionCoordinator().getJdbcCoordinator().getResultSetReturn().extract(st);
            try {
                rs.next();
                Long id = rs.getLong(1);
                return id;
            }
            finally {
                session.getTransactionCoordinator().getJdbcCoordinator().release(rs, st);
            }
        }
        finally {
            session.getTransactionCoordinator().getJdbcCoordinator().release(st);
        }
    }
    catch (SQLException sqle) {
        throw session.getFactory().getSQLExceptionHelper().convert(
                sqle,
                "could not get next sequence value",
                sql2
        );
    }
}
```

#### Usage
We can now use our newly created generator much like we use default Hibernate generators. The difference is we provide
full class name of our generator in the `strategy` parameter.
```
@GenericGenerator(name = "gen_double_sequence", strategy = "com.kainos.learn.hibseq.db.id.generator.DoubleSequenceGenerator",
        parameters = {
                @Parameter(name = "sequence", value = "gen_double_seq"),
                @Parameter(name = "second_sequence", value = "gen_double_seq_2")
        })
public class GenericGeneratorDoubleSequence extends DomainEntity {
```
Additionally we provide a parameter with the name of our second sequence. We can now use the generator on our String ID column.
```
@Id
@Column(name = "ID")
@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "gen_double_sequence")
private String id;
```

### Use cases

So what generation strategy should you use? As is the case with almost everything - it depends. There are a few factors
that you should consider when choosing a sequence ID generation strategy.

#### Consistency
If your solution provides multiple sequences or more then one databases then it makes sense to pick one ID generation 
strategy for everything. If you are extending an existing solution it is worth considering whether the strategy that is 
already in use works for you. This prevents a lot of confusion. Its not uncommon to see developers assume that only one 
strategy is in use. If it turns out that is not the case and someone uses a sequence configured with `HiLo` strategy as if 
it were a plain sequence when developing a database patch things will go wrong. In this case you might end up creating 
ID conflicts since someone was not aware that one of the sequences was using different strategy.

#### Interoperability
It is worth considering what kind of usages will your database see. Will your database be used by many applications? 
Will it be used by 3rd party suppliers? It would be also worth considering that in the future manual 
fix scripts might be run on your database. To minimize the chance of generating ID conflicts it is best to use
ID generation strategies that are compatible with other generation strategies that are likely to be used. 

`HiLo` strategy does not perform well in that regard. If you use one DB sequence using `HiLo` strategy and any of 
other standard strategies covered here you will generate duplicate ID values eventually.

`Pooled` optimizer works well with ordinary sequence ID generation strategy in Hibernate 4.3.11.Final and forwards. 
In previous implementations however [it did not](https://hibernate.atlassian.net/browse/HHH-9287). 
`PooledLo` optimizer also works well with ordinary sequence ID generation strategy. Of course when 
using standard Hibernate sequence generator on a sequence created for use with pooled optimizers gaps in ID values 
will be created.

The standard sequence generation strategy without optimization can be used on sequences created for `Pooled` and `PooledLo`
optimization. Only issue this creates are gaps in the generated ID values. This makes it the safest strategy 
when its not clear how other applications will be using the underlying database.

#### Ordering
Sometimes we need to generate values in order (i.e. generated values are always bigger or smaller then the previous ones).
This is achievable using a sequence that is not cached with standard generation strategy. However it is worth noting 
that when using multiple applications that query a single sequence using any optimization strategy using `increment_size > 1`
ordering cannot be assured.

#### Performance
An important aspect for most use cases would be the performance of different strategies. Here is a sample time output given in milliseconds 
of creating and flushing 200000 entities with ID values generated by a given sequence:
```
Avg time of Pooled sequence: 13948
Avg time of Ordinary sequence: 24732
Avg time of PooledLo sequence: 14433
Avg time of HiLo sequence: 13664
```
Note that the times may vary depending on the size of the entities, database and database drivers used. For this test sequences
were not using cache on the database side. Hibernate auto generation does not allow to insert additional custom parameters
for optimized sequences. It is not possible to set a cache size with `@Parameter(name = "parameters", value = "CACHE 100")`.
However in this case using cache on the database side with ordinary sequence did not improve performance at all:
```
Avg time of Ordinary sequence: 24897
```
This indicates that the bottleneck for time performance in this case is the application itself, not the database. 
This of course might change when multiple applications query a single database or when executing queries directly with a
SQL script.

## Sources
* [Source code](https://github.com/hibernate/hibernate-orm/tree/4.3.11.Final/)
* [Hibernate Reference](https://docs.jboss.org/hibernate/orm/3.3/reference/en/html/mapping.html#mapping-declaration-id)
* [Vlad Mihalcea - Hibernate Hidden Gem: The Pooled-Lo Optimizer](https://dzone.com/articles/hibernate-hidden-gem-pooled-lo)
