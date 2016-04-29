# Hibernate Sequences WIP

## Usage
* Make sure you have Postgres running and create a database for this project
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

## Automatically generated ID values with sequences by Hibernate - example with Dropwizard 0.9.2

### Introduction
Sequences allow for easy and flexible way of generating identifiers for database entities. Not all
databases support sequences, but when they do, sequences are oftentimes the go to strategy for generating object identifiers.

Hibernate is a very popular object mapping framework for Java. Thanks to its popularity there are a lot of
resources online and you get the assurance that it is tried and tested. However this should not stop us from
taking a deeper look into what we are actually using. Especially if we are setting up a project and we suspect
that our usage of hibernate will get replicated across the project for consistency reasons by other developers.

We are going to explore different ways of generating ID values with sequences and consider their pros and cons. To demonstrate
different ID generation strategies I've set up a sample Dropwizard service that persists entities comprised of a single ID column.

In this example I'm using Dropwizard 0.9.2. This particular version of Dropwizard uses hibernate 4.3.11.Final under the hood.

### Main sequence based generation strategies

#### Sequence with no optimizer 
Uses a sequence to determine the next ID value. The ID values are directly derived from the current sequence value.

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
from being a bottleneck in batch inserts. The database ensures that the values fetched from a sequence are never duplicate.

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

Since this method of generating ID values forces the HiLoOptimizer to hold `high` abd `low` values in memory the method 
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
This one operates similarly to Pooled optimizer, but instead of keeping the last value in the 
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

### Use cases

## TODO
* custom ID generation strategies
* add script/code to make a few batch inserts and get the average time for perf tests
* verify SequenceHiLoGenerator vs SequenceGenerator + HiLoOptimizer //SequenceHiLoGenerator uses LegacyHiLoAlgorithmOptimizer, 
would be usefull to write a few things about that, since there seem to be a few small differences
* Test perf with sequence caching as well!
* verify that SequenceIdentity strategy does not work on postgres // comments in the current implementation state that it doesn't
