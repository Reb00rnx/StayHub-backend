package com.stayhub.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.stayhub")
@EntityScan(basePackages = "com.stayhub")
@EnableJpaRepositories(basePackages = "com.stayhub")
public class StayHubApplication {

    public static void main(String[] args) {
        SpringApplication.run(StayHubApplication.class, args);
    }
}
