package com.example.nordicelectronics.config;

import org.testcontainers.containers.PostgreSQLContainer;

public class TestDatabaseContainer {

    private static final PostgreSQLContainer<?> POSTGRES_CONTAINER;

    static {
        POSTGRES_CONTAINER = new PostgreSQLContainer<>("postgres:17")
                .withDatabaseName("nordic_test")
                .withUsername("test_user")
                .withPassword("test_pass")
                .withReuse(true);                 // Reuse container between test runs
                //.withInitScript("db/test-schema.sql");  // Load your schema

        POSTGRES_CONTAINER.start();

        System.out.println("🚀 PostgreSQL Testcontainer started!");
        System.out.println("📍 JDBC URL: " + POSTGRES_CONTAINER.getJdbcUrl());
    }

    private TestDatabaseContainer() {
        throw new IllegalStateException("Cannot instantiate singleton");
    }

    public static PostgreSQLContainer<?> getInstance() {
        return POSTGRES_CONTAINER;
    }

    // Helper methods
    public static String getJdbcUrl() {
        return POSTGRES_CONTAINER.getJdbcUrl();
    }

    public static String getUsername() {
        return POSTGRES_CONTAINER.getUsername();
    }

    public static String getPassword() {
        return POSTGRES_CONTAINER.getPassword();
    }
}