package com.example.nordicelectronics.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = "com.example.nordicelectronics.repositories.sql")
public class JpaConfig {

}

