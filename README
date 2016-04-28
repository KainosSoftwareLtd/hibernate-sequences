# Hibernate Sequences WIP

## Usage
* Make sure you have Postgres running and create a database for this project
* Run the project with `./go`
* You can fiddle around by hitting the endpoints that create test entities and use the sequences as a consequence.

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

### Sequence based generation strategies

* Sequence with no optimizer - uses a sequence to determine the next ID value. The ID values are directly derived from the current sequence value.
* Sequence with HiLo optimizer - uses the database sequence to store and retrieve the "high" number and generates
* Pooled
* PooledLo
* SequenceIdentity?? (according to a bit outdated reference supported only on oracle - verify this)



## TODO
* add script/code to make a few batch inserts and get the average time for perf tests
* verify that SequenceIdentity strategy does not work on postgres
* verify SequenceHiLoGenerator vs SequenceGenerator + HiLoOptimizer
* Test perf with sequence caching as well!