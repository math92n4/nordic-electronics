package com.example.nordicelectronics.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@ConditionalOnProperty(name = "spring.data.mongodb.enabled", havingValue = "true", matchIfMissing = true)
@EnableMongoRepositories(basePackages = "com.example.nordicelectronics.repositories.mongodb")
@EnableMongoAuditing
public class MongoConfig {

}

