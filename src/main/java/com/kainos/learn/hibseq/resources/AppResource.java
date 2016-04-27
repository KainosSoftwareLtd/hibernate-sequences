package com.kainos.learn.hibseq.resources;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.codahale.metrics.annotation.Timed;
import com.google.common.collect.Lists;
import com.kainos.learn.hibseq.db.*;
import com.kainos.learn.hibseq.domain.*;
import io.dropwizard.hibernate.UnitOfWork;
import org.eclipse.jetty.http.HttpParser;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import java.util.stream.Stream;

@Path("/hibSeq")
public class AppResource {

    static final MetricRegistry metrics = new MetricRegistry();

    private GeneratedValueSequenceDao generatedValueSequenceDao;
    private GenericGeneratorSequenceDao genericGeneratorSequenceDao;
    private GenericGeneratorSeqHiLoDao genericGeneratorSeqHiLoDao;
    private GenericGeneratorSeqPooledDao genericGeneratorSeqPooledDao;
    private GenericGeneratorSeqPooledLoDao genericGeneratorSeqPooledLoDao;

    public AppResource(GeneratedValueSequenceDao generatedValueSequenceDao,
                       GenericGeneratorSequenceDao genericGeneratorSequenceDao,
                       GenericGeneratorSeqHiLoDao genericGeneratorSeqHiLoDao,
                       GenericGeneratorSeqPooledDao genericGeneratorSeqPooledDao,
                       GenericGeneratorSeqPooledLoDao genericGeneratorSeqPooledLoDao) {

        this.generatedValueSequenceDao = generatedValueSequenceDao;
        this.genericGeneratorSequenceDao = genericGeneratorSequenceDao;
        this.genericGeneratorSeqHiLoDao = genericGeneratorSeqHiLoDao;
        this.genericGeneratorSeqPooledDao = genericGeneratorSeqPooledDao;
        this.genericGeneratorSeqPooledLoDao = genericGeneratorSeqPooledLoDao;
    }

    @GET
    @Path("/sequenceGen/{num_ids}")
    @UnitOfWork
    public String generatedValueSeq(@PathParam("num_ids") Long numIds) {
        Function<LongStream, Stream<DomainEntity>> func = e -> this.getCreatedGeneratedValueSequencesStream(e);

        String result = getResultWithTime(LongStream.range(0, numIds), func);
        return result;
    }

    @GET
    @Path("/genericSeq/{num_ids}")
    @UnitOfWork
    public String genericGeneratorSeq(@PathParam("num_ids") Long numIds) {
        Function<LongStream, Stream<DomainEntity>> func = e -> this.getCreatedGenericGeneratorSequencesStream(e);

        String result = getResultWithTime(LongStream.range(0, numIds), func);
        return result;
    }

    @GET
    @Path("/genericSeqHiLo/{num_ids}")
    @UnitOfWork
    public String genericGeneratorSeqHiLo(@PathParam("num_ids") Long numIds) {
        Function<LongStream, Stream<DomainEntity>> func = e -> this.getCreatedGenericGeneratorSeqHiLoSequencesStream(e);

        String result = getResultWithTime(LongStream.range(0, numIds), func);
        return result;
    }

    @GET
    @Path("/genericSeqPooled/{num_ids}")
    @UnitOfWork
    public String genericGeneratorSeqPooled(@PathParam("num_ids") Long numIds) {
        Function<LongStream, Stream<DomainEntity>> func = e -> this.getCreatedGenericGeneratorSeqPooledSequencesStream(e);

        String result = getResultWithTime(LongStream.range(0, numIds), func);
        return result;
    }

    @GET
    @Path("/genericSeqPooledLo/{num_ids}")
    @UnitOfWork
    public String genericGeneratorSeqPooledLo(@PathParam("num_ids") Long numIds) {
        Function<LongStream, Stream<DomainEntity>> func = e -> this.getCreatedGenericGeneratorSeqPooledLoSequencesStream(e);

        String result = getResultWithTime(LongStream.range(0, numIds), func);
        return result;
    }

    private String getResultWithTime(LongStream range, Function<LongStream, Stream<DomainEntity>> func) {
        long startTime = System.nanoTime();
        String result = func.apply(range).map(e -> e.toString()).collect(Collectors.joining(", "));
        long endTime = System.nanoTime();

        long duration = (endTime - startTime) / 1000000;
        return  result + ": " + duration + " milliseconds";
    }

    // SequenceGenerator - strategy = sequence - allocationSize = default(50)
    private Stream<DomainEntity> getCreatedGeneratedValueSequencesStream(LongStream range) {
        return range.mapToObj(l -> generatedValueSequenceDao.persist(new GeneratedValueSequence()));
    }

    // GenericGenerator - strategy = sequence - allocationSize = default(1)
    private Stream<DomainEntity> getCreatedGenericGeneratorSequencesStream(LongStream range) {
        return range.mapToObj(l -> genericGeneratorSequenceDao.persist(new GenericGeneratorSequence()));
    }

    // GenericGenerator - strategy = seqhilo
    private Stream<DomainEntity> getCreatedGenericGeneratorSeqHiLoSequencesStream(LongStream range) {
        return range.mapToObj(l -> genericGeneratorSeqHiLoDao.persist(new GenericGeneratorSeqHiLo()));
    }

    // GenericGenerator - strategy = pooled
    private Stream<DomainEntity> getCreatedGenericGeneratorSeqPooledSequencesStream(LongStream range) {
        return range.mapToObj(l -> genericGeneratorSeqPooledDao.persist(new GenericGeneratorSeqPooled()));
    }

    // GenericGenerator - strategy = pooledlo
    private Stream<DomainEntity> getCreatedGenericGeneratorSeqPooledLoSequencesStream(LongStream range) {
        return range.mapToObj(l -> genericGeneratorSeqPooledLoDao.persist(new GenericGeneratorSeqPooledLo()));
    }
}
