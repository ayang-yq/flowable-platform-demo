package com.flowable.platform.test.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.flowable.platform.test.util.FixtureLoader;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * Main test configuration with common beans for testing.
 */
@TestConfiguration
public class TestConfig {

    @Bean
    public ObjectMapper testObjectMapper() {
        return new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Bean
    public FixtureLoader fixtureLoader(ObjectMapper objectMapper) {
        return new FixtureLoader(objectMapper);
    }
}
