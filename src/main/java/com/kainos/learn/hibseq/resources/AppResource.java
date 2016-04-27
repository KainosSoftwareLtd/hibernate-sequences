package com.kainos.learn.hibseq.resources;

import com.codahale.metrics.annotation.Timed;
import com.kainos.learn.hibseq.db.*;
import com.kainos.learn.hibseq.domain.*;
import io.dropwizard.hibernate.UnitOfWork;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import java.util.stream.Stream;

@Path("/hibSeq")
public class AppResource {

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
        Stream<GeneratedValueSequence> entities = getCreatedGeneratedValueSequencesStream(LongStream.range(0, numIds));

        return entities.map(e -> e.toString()).collect(Collectors.joining(", "));
    }

    @GET
    @Path("/genericSeq/{num_ids}")
    @UnitOfWork
    public String genericGeneratorSeq(@PathParam("num_ids") Long numIds) {
        Stream<GenericGeneratorSequence> entities = getCreatedGenericGeneratorSequencesStream(LongStream.range(0, numIds));

        return entities.map(e -> e.toString()).collect(Collectors.joining(", "));
    }

    @GET
    @Path("/genericSeqHiLo/{num_ids}")
    @UnitOfWork
    public String genericGeneratorSeqHiLo(@PathParam("num_ids") Long numIds) {
        Stream<GenericGeneratorSeqHiLo> entities = getCreatedGenericGeneratorSeqHiLoSequencesStream(LongStream.range(0, numIds));

        return entities.map(e -> e.toString()).collect(Collectors.joining(", "));
    }

    @GET
    @Path("/genericSeqPooled/{num_ids}")
    @UnitOfWork
    public String genericGeneratorSeqPooled(@PathParam("num_ids") Long numIds) {
        Stream<GenericGeneratorSeqPooled> entities = getCreatedGenericGeneratorSeqPooledSequencesStream(LongStream.range(0, numIds));

        return entities.map(e -> e.toString()).collect(Collectors.joining(", "));
    }

    @GET
    @Path("/genericSeqPooledLo/{num_ids}")
    @UnitOfWork
    public String genericGeneratorSeqPooledLo(@PathParam("num_ids") Long numIds) {
        Stream<GenericGeneratorSeqPooledLo> entities = getCreatedGenericGeneratorSeqPooledLoSequencesStream(LongStream.range(0, numIds));

        return entities.map(e -> e.toString()).collect(Collectors.joining(", "));
    }

    @Timed(name = "SequenceGenerator - strategy = sequence - allocationSize = default(50)")
    private Stream<GeneratedValueSequence> getCreatedGeneratedValueSequencesStream(LongStream range) {
        return range.mapToObj(l -> generatedValueSequenceDao.persist(new GeneratedValueSequence()));
    }

    @Timed(name = "GenericGenerator - strategy = sequence - allocationSize = default(1)")
    private Stream<GenericGeneratorSequence> getCreatedGenericGeneratorSequencesStream(LongStream range) {
        return range.mapToObj(l -> genericGeneratorSequenceDao.persist(new GenericGeneratorSequence()));
    }

    @Timed(name = "GenericGenerator - strategy = seqhilo")
    private Stream<GenericGeneratorSeqHiLo> getCreatedGenericGeneratorSeqHiLoSequencesStream(LongStream range) {
        return range.mapToObj(l -> genericGeneratorSeqHiLoDao.persist(new GenericGeneratorSeqHiLo()));
    }

    @Timed(name = "GenericGenerator - strategy = pooled")
    private Stream<GenericGeneratorSeqPooled> getCreatedGenericGeneratorSeqPooledSequencesStream(LongStream range) {
        return range.mapToObj(l -> genericGeneratorSeqPooledDao.persist(new GenericGeneratorSeqPooled()));
    }

    @Timed(name = "GenericGenerator - strategy = pooledlo")
    private Stream<GenericGeneratorSeqPooledLo> getCreatedGenericGeneratorSeqPooledLoSequencesStream(LongStream range) {
        return range.mapToObj(l -> genericGeneratorSeqPooledLoDao.persist(new GenericGeneratorSeqPooledLo()));
    }
}
