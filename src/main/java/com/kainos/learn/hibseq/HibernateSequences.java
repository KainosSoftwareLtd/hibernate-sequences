package com.kainos.learn.hibseq;

import com.codahale.metrics.servlets.MetricsServlet;
import com.kainos.learn.hibseq.db.*;
import com.kainos.learn.hibseq.domain.*;
import io.dropwizard.Application;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.hibernate.HibernateBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import com.kainos.learn.hibseq.resources.AppResource;

public class HibernateSequences extends Application<com.kainos.learn.hibseq.config.HibernateSequences> {

    private static final String NAME = "hibernate-sequences";

    private final HibernateBundle<com.kainos.learn.hibseq.config.HibernateSequences> hibernate = new HibernateBundle<com.kainos.learn.hibseq.config.HibernateSequences>(
            GeneratedValueSequence.class,
            GenericGeneratorSequence.class,
            GenericGeneratorSeqHiLo.class,
            GenericGeneratorSeqPooled.class,
            GenericGeneratorSeqPooledLo.class,
            GenericGeneratorDoubleSequence.class) {

        @Override
        public DataSourceFactory getDataSourceFactory(com.kainos.learn.hibseq.config.HibernateSequences configuration) {
            return configuration.getDatabaseAppDataSourceFactory();
        }
    };

    public static void main(String[] args) throws Exception {
        new HibernateSequences().run(args);
    }

    @Override
    public void initialize(Bootstrap<com.kainos.learn.hibseq.config.HibernateSequences> bootstrap) {
        bootstrap.addBundle(hibernate);
    }

    @Override
    public void run(com.kainos.learn.hibseq.config.HibernateSequences configuration, Environment environment) throws Exception {

        //DAO
        GeneratedValueSequenceDao generatedValueSequenceDao = new GeneratedValueSequenceDao(hibernate.getSessionFactory());
        GenericGeneratorSequenceDao genericGeneratorSequenceDao = new GenericGeneratorSequenceDao(hibernate.getSessionFactory());
        GenericGeneratorSeqHiLoDao genericGeneratorSeqHiLoDao = new GenericGeneratorSeqHiLoDao(hibernate.getSessionFactory());
        GenericGeneratorSeqPooledDao genericGeneratorSeqPooledDao = new GenericGeneratorSeqPooledDao(hibernate.getSessionFactory());
        GenericGeneratorSeqPooledLoDao genericGeneratorSeqPooledLoDao = new GenericGeneratorSeqPooledLoDao(hibernate.getSessionFactory());
        GenericGeneratorDoubleSequenceDao genericGeneratorDoubleSequenceDao = new GenericGeneratorDoubleSequenceDao(hibernate.getSessionFactory());

        //Resources
        environment.jersey().register(new AppResource(generatedValueSequenceDao, genericGeneratorSequenceDao,
                genericGeneratorSeqHiLoDao, genericGeneratorSeqPooledDao, genericGeneratorSeqPooledLoDao,
                genericGeneratorDoubleSequenceDao));
    }

    @Override
    public String getName() {
        return NAME;
    }
}
