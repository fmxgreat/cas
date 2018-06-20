package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.couchdb.EventCouchDbRepository;
import org.apereo.cas.couchdb.core.CouchDbConnectorFactory;
import org.apereo.cas.support.events.CasEventRepository;
import org.apereo.cas.support.events.CouchDbCasEventRepository;

import lombok.val;
import org.ektorp.CouchDbConnector;
import org.ektorp.CouchDbInstance;
import org.ektorp.impl.ObjectMapperFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link CouchDbEventsConfiguration}, defines certain beans via configuration
 * while delegating some to Spring namespaces inside the context config file.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
@Configuration("couchDbEventsConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CouchDbEventsConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("defaultObjectMapperFactory")
    private ObjectMapperFactory objectMapperFactory;

    @Autowired
    @Qualifier("eventCouchDbFactory")
    private CouchDbConnectorFactory eventCouchDbFactory;

    @ConditionalOnMissingBean(name = "eventCouchDbImnstance")
    @RefreshScope
    @Bean
    public CouchDbInstance eventCouchDbInstance() {
        return eventCouchDbFactory.createInstance();
    }

    @ConditionalOnMissingBean(name = "eventCouchDbConnector")
    @RefreshScope
    @Bean
    public CouchDbConnector eventCouchDbConnector() {
        return eventCouchDbFactory.createConnector();
    }

    @ConditionalOnMissingBean(name = "couchDbEventRepository")
    @Bean
    @RefreshScope
    public EventCouchDbRepository couchDbEventRepository() {
        val repository = new EventCouchDbRepository(eventCouchDbFactory.create(), casProperties.getEvents().getCouchDb().isCreateIfNotExists());
        repository.initStandardDesignDocument();
        return repository;
    }

    @ConditionalOnMissingBean(name = "eventCouchDbFactory")
    @Bean
    @RefreshScope
    public CouchDbConnectorFactory eventCouchDbFactory() {
        return new CouchDbConnectorFactory(casProperties.getEvents().getCouchDb(), objectMapperFactory);
    }

    @ConditionalOnMissingBean(name = "couchDbCasEventRepository")
    @Bean
    @RefreshScope
    public CasEventRepository casEventRepository() {
        return new CouchDbCasEventRepository(couchDbEventRepository(), casProperties.getEvents().getCouchDb().isAsyncronous());
    }
}
