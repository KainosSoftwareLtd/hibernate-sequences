package com.kainos.learn.hibseq.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import io.dropwizard.db.DataSourceFactory;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class HibernateSequences extends Configuration {

    @Valid
    @NotNull
    @JsonProperty
    private DataSourceFactory databaseApp = new DataSourceFactory();

    public DataSourceFactory getDatabaseAppDataSourceFactory() {
        return databaseApp;
    }

    @Valid
    @NotNull
    @JsonProperty
    private ApplicationConfig applicationConfig = new ApplicationConfig();

    public ApplicationConfig getApplicationConfig() {
        return applicationConfig;
    }
}
