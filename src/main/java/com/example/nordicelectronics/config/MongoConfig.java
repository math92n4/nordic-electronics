package com.example.nordicelectronics.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(basePackages = "com.example.nordicelectronics.repositories.mongodb")
public class MongoConfig {

}

