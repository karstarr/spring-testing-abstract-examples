package com.griddynamics.akarsakov;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@EnableMongoRepositories
public class TestingSpringBootApplication {

    public static void main(String[] args) {
        SpringApplication.run(TestingSpringBootApplication.class, args);
    }
}
