package com.flowable.platform.test.config;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/**
 * WireMock configuration for external HTTP service stubbing in tests.
 */
@TestConfiguration
public class WireMockConfig {

    public static final int WIREMOCK_PORT = 8089;

    @Bean
    @Primary
    public WireMockServer wireMockServer() {
        WireMockServer wireMockServer = new WireMockServer(
            WireMockConfiguration.wireMockConfig().port(WIREMOCK_PORT)
        );
        wireMockServer.start();
        return wireMockServer;
    }
}
