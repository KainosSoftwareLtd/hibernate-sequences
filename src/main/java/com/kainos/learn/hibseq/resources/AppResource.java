package com.kainos.learn.hibseq.resources;

import com.kainos.learn.hibseq.db.*;
import com.kainos.learn.hibseq.domain.*;
import io.dropwizard.hibernate.UnitOfWork;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import java.util.stream.Stream;

@Path("/hibSeq")
public class AppResource {

    private static Logger LOGGER = LoggerFactory.getLogger(AppResource.class);

    private GeneratedValueSequenceDao generatedValueSequenceDao;
    private GenericGeneratorSequenceDao genericGeneratorSequenceDao;
    private GenericGeneratorSeqHiLoDao genericGeneratorSeqHiLoDao;
    private GenericGeneratorSeqPooledDao genericGeneratorSeqPooledDao;
    private GenericGeneratorSeqPooledLoDao genericGeneratorSeqPooledLoDao;
    private GenericGeneratorDoubleSequenceDao genericGeneratorDoubleSequenceDao;

    public AppResource(GeneratedValueSequenceDao generatedValueSequenceDao,
                       GenericGeneratorSequenceDao genericGeneratorSequenceDao,
                       GenericGeneratorSeqHiLoDao genericGeneratorSeqHiLoDao,
                       GenericGeneratorSeqPooledDao genericGeneratorSeqPooledDao,
                       GenericGeneratorSeqPooledLoDao genericGeneratorSeqPooledLoDao,
                       GenericGeneratorDoubleSequenceDao genericGeneratorDoubleSequenceDao) {

        this.generatedValueSequenceDao = generatedValueSequenceDao;
        this.genericGeneratorSequenceDao = genericGeneratorSequenceDao;
        this.genericGeneratorSeqHiLoDao = genericGeneratorSeqHiLoDao;
        this.genericGeneratorSeqPooledDao = genericGeneratorSeqPooledDao;
        this.genericGeneratorSeqPooledLoDao = genericGeneratorSeqPooledLoDao;
        this.genericGeneratorDoubleSequenceDao = genericGeneratorDoubleSequenceDao;
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

    @GET
    @Path("/genericDoubleSeq/{num_ids}")
    @UnitOfWork
    public String genericGeneratorDoubleSeq(@PathParam("num_ids") Long numIds) {
        Function<LongStream, Stream<DomainEntity>> func = e -> this.getCreatedGenericGeneratorDoubleSequencesStream(e);

        String result = getResultWithTime(LongStream.range(0, numIds), func);
        return result;
    }

    @GET
    @Path("/perf/{num_ids}/{num_passes}")
    @UnitOfWork
    public String performanceCheck(@PathParam("num_ids") Long numIds, @PathParam("num_passes") Long numPasses) {
        LOGGER.info(String.format("Starting perf tests: Num entries per pass: %d, Num passes: %d", numIds, numPasses));

        Map<String, Function<LongStream, Stream<DomainEntity>>> insertFunctions = new HashMap<>();
        insertFunctions.put("Ordinary sequence", e -> this.getCreatedGenericGeneratorSequencesStream(e));
        insertFunctions.put("HiLo sequence", e -> this.getCreatedGenericGeneratorSeqHiLoSequencesStream(e));
        insertFunctions.put("Pooled sequence", e -> this.getCreatedGenericGeneratorSeqPooledSequencesStream(e));
        insertFunctions.put("PooledLo sequence", e -> this.getCreatedGenericGeneratorSeqPooledLoSequencesStream(e));

        String result = insertFunctions.entrySet().stream().sequential()
                .map(entry -> perfTest(entry.getKey(), entry.getValue(), numIds, numPasses))
                .collect(Collectors.joining("\n"));

        return result;
    }

    private String getResultWithTime(LongStream range, Function<LongStream, Stream<DomainEntity>> func) {
        long startTime = System.nanoTime();
        String result = func.apply(range).map(e -> e.toString()).collect(Collectors.joining(", "));
        long endTime = System.nanoTime();

        long duration = (endTime - startTime) / 1000000;
        return  result + ": " + duration + " milliseconds";
    }

    private String perfTest(String seqName, Function<LongStream, Stream<DomainEntity>> generationFunction, Long numIds, Long numPasses) {
        LOGGER.info(String.format("Starting perf test for %s", seqName));
        // warmup pass
        getTime(LongStream.range(0, numIds), generationFunction);

        Long timeSum = LongStream.range(0, numPasses).boxed().sequential()
                .map(i -> getTime(LongStream.range(0, numIds), generationFunction))
                .collect(Collectors.summingLong(n -> n));

        return "Avg time of " + seqName + ": " + (timeSum / numPasses);

    }

    public Long getTime(LongStream range, Function<LongStream, Stream<DomainEntity>> func) {
        long startTime = System.nanoTime();
        func.apply(range).map(e -> e.toString()).collect(Collectors.toList());
        long endTime = System.nanoTime();
        long duration = (endTime - startTime) / 1000000;

        LOGGER.info(String.format("Time needed for inserts: %d", duration));
        return  duration;
    }

    // SequenceGenerator - strategy = sequence - allocationSize = default(50)
    private Stream<DomainEntity> getCreatedGeneratedValueSequencesStream(LongStream range) {
        Stream<DomainEntity> result = range.mapToObj(l -> generatedValueSequenceDao.persist(new GeneratedValueSequence()));
        generatedValueSequenceDao.flushClearSession();
        return result;
    }

    // GenericGenerator - strategy = sequence - allocationSize = default(1)
    private Stream<DomainEntity> getCreatedGenericGeneratorSequencesStream(LongStream range) {
        Stream<DomainEntity> result = range.mapToObj(l -> genericGeneratorSequenceDao.persist(new GenericGeneratorSequence()));
        genericGeneratorSequenceDao.flushClearSession();
        return result;
    }

    // GenericGenerator - strategy = seqhilo
    private Stream<DomainEntity> getCreatedGenericGeneratorSeqHiLoSequencesStream(LongStream range) {
        Stream<DomainEntity> result = range.mapToObj(l -> genericGeneratorSeqHiLoDao.persist(new GenericGeneratorSeqHiLo()));
        genericGeneratorSeqHiLoDao.flushClearSession();
        return result;
    }

    // GenericGenerator - strategy = pooled
    private Stream<DomainEntity> getCreatedGenericGeneratorSeqPooledSequencesStream(LongStream range) {
        Stream<DomainEntity> result = range.mapToObj(l -> genericGeneratorSeqPooledDao.persist(new GenericGeneratorSeqPooled()));
        genericGeneratorSeqPooledDao.flushClearSession();
        return result;
    }

    // GenericGenerator - strategy = pooledlo
    private Stream<DomainEntity> getCreatedGenericGeneratorSeqPooledLoSequencesStream(LongStream range) {
        Stream<DomainEntity> result = range.mapToObj(l -> genericGeneratorSeqPooledLoDao.persist(new GenericGeneratorSeqPooledLo()));
        genericGeneratorSeqPooledLoDao.flushClearSession();
        return result;
    }

    // GenericGenerator - strategy = double seq custom
    private Stream<DomainEntity> getCreatedGenericGeneratorDoubleSequencesStream(LongStream range) {
        Stream<DomainEntity> result = range.mapToObj(l -> genericGeneratorDoubleSequenceDao.persist(new GenericGeneratorDoubleSequence()));
        genericGeneratorDoubleSequenceDao.flushClearSession();
        return result;
    }
}
