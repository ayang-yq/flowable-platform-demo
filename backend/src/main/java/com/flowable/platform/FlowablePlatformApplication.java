package com.flowable.platform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication(
    exclude = {
        org.flowable.spring.boot.RestApiAutoConfiguration.class
    }
)
@EnableAsync
public class FlowablePlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(FlowablePlatformApplication.class, args);
    }
}
