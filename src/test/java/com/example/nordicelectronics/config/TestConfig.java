package com.example.nordicelectronics.config;

import org.springframework.boot.test.context.TestConfiguration;

/**
 * Test configuration class.
 * MongoDB-related beans are disabled via @ConditionalOnProperty on the source classes
 * and spring.data.mongodb.enabled=false in application-test.properties.
 */
@TestConfiguration
public class TestConfig {
}