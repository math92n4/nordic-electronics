package com.example.nordicelectronics.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.tags.Tag;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
public class SwaggerConfig {

    // Define the desired order of tags (grouped by database type)
    private static final List<String> TAG_ORDER = List.of(
            // PostgreSQL Controllers
            "PostgreSQL Authentication Controller",
            "PostgreSQL Product Controller",
            "PostgreSQL Order Controller",
            "PostgreSQL Payment Controller",
            "PostgreSQL Review Controller",
            "PostgreSQL Coupon Controller",
            "PostgreSQL Category Controller",
            "PostgreSQL Brand Controller",
            "PostgreSQL Warehouse Controller",
            "PostgreSQL Warehouse Product Controller",
            "PostgreSQL Warranty Controller",
            "PostgreSQL Address Controller",
            "Stripe Controller",
            // MongoDB Controllers
            "MongoDB Product Controller",
            "MongoDB Order Controller",
            "MongoDB Payment Controller",
            "MongoDB Review Controller",
            "MongoDB Coupon Controller",
            "MongoDB Category Controller",
            "MongoDB Brand Controller",
            "MongoDB Warehouse Controller",
            "MongoDB Warranty Controller",
            "MongoDB Address Controller",
            "MongoDB User Controller",
            // Neo4j Controllers
            "Neo4j Product Controller",
            "Neo4j Order Controller",
            "Neo4j Payment Controller",
            "Neo4j Review Controller",
            "Neo4j Coupon Controller",
            "Neo4j Category Controller",
            "Neo4j Brand Controller",
            "Neo4j Warehouse Controller",
            "Neo4j Warranty Controller",
            "Neo4j Address Controller",
            "Neo4j User Controller",
            // Data Migration
            "Data Migration Controller"
    );

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("Nordic Electronics API")
                        .version("1.0")
                        .description("API documentation for Nordic Electronics Webshop - Multi-database architecture supporting PostgreSQL, MongoDB, and Neo4j"))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName, new SecurityScheme()
                                .name(securitySchemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")))
                .tags(List.of(
                        // PostgreSQL Controllers
                        new Tag().name("PostgreSQL Authentication Controller").description("Authentication and registration endpoints (PostgreSQL)"),
                        new Tag().name("PostgreSQL Product Controller").description("Product management endpoints (PostgreSQL)"),
                        new Tag().name("PostgreSQL Order Controller").description("Order management endpoints (PostgreSQL)"),
                        new Tag().name("PostgreSQL Payment Controller").description("Payment processing endpoints (PostgreSQL)"),
                        new Tag().name("PostgreSQL Review Controller").description("Product review endpoints (PostgreSQL)"),
                        new Tag().name("PostgreSQL Coupon Controller").description("Coupon management endpoints (PostgreSQL)"),
                        new Tag().name("PostgreSQL Category Controller").description("Category management endpoints (PostgreSQL)"),
                        new Tag().name("PostgreSQL Brand Controller").description("Brand management endpoints (PostgreSQL)"),
                        new Tag().name("PostgreSQL Warehouse Controller").description("Warehouse management endpoints (PostgreSQL)"),
                        new Tag().name("PostgreSQL Warehouse Product Controller").description("Warehouse product inventory endpoints (PostgreSQL)"),
                        new Tag().name("PostgreSQL Warranty Controller").description("Warranty management endpoints (PostgreSQL)"),
                        new Tag().name("PostgreSQL Address Controller").description("Address management endpoints (PostgreSQL)"),
                        new Tag().name("Stripe Controller").description("Stripe payment integration endpoints"),
                        // MongoDB Controllers
                        new Tag().name("MongoDB Product Controller").description("Product management endpoints (MongoDB)"),
                        new Tag().name("MongoDB Order Controller").description("Order management endpoints (MongoDB)"),
                        new Tag().name("MongoDB Payment Controller").description("Payment processing endpoints (MongoDB)"),
                        new Tag().name("MongoDB Review Controller").description("Product review endpoints (MongoDB)"),
                        new Tag().name("MongoDB Coupon Controller").description("Coupon management endpoints (MongoDB)"),
                        new Tag().name("MongoDB Category Controller").description("Category management endpoints (MongoDB)"),
                        new Tag().name("MongoDB Brand Controller").description("Brand management endpoints (MongoDB)"),
                        new Tag().name("MongoDB Warehouse Controller").description("Warehouse management endpoints (MongoDB)"),
                        new Tag().name("MongoDB Warranty Controller").description("Warranty management endpoints (MongoDB)"),
                        new Tag().name("MongoDB Address Controller").description("Address management endpoints (MongoDB)"),
                        new Tag().name("MongoDB User Controller").description("User management endpoints (MongoDB)"),
                        // Neo4j Controllers
                        new Tag().name("Neo4j Product Controller").description("Product management endpoints (Neo4j)"),
                        new Tag().name("Neo4j Order Controller").description("Order management endpoints (Neo4j)"),
                        new Tag().name("Neo4j Payment Controller").description("Payment processing endpoints (Neo4j)"),
                        new Tag().name("Neo4j Review Controller").description("Product review endpoints (Neo4j)"),
                        new Tag().name("Neo4j Coupon Controller").description("Coupon management endpoints (Neo4j)"),
                        new Tag().name("Neo4j Category Controller").description("Category management endpoints (Neo4j)"),
                        new Tag().name("Neo4j Brand Controller").description("Brand management endpoints (Neo4j)"),
                        new Tag().name("Neo4j Warehouse Controller").description("Warehouse management endpoints (Neo4j)"),
                        new Tag().name("Neo4j Warranty Controller").description("Warranty management endpoints (Neo4j)"),
                        new Tag().name("Neo4j Address Controller").description("Address management endpoints (Neo4j)"),
                        new Tag().name("Neo4j User Controller").description("User management endpoints (Neo4j)"),
                        // Data Migration
                        new Tag().name("Data Migration Controller").description("Data migration endpoints between PostgreSQL, MongoDB, and Neo4j")
                ));
    }

    // Shared tag sorter customizer
    private OpenApiCustomizer tagSorterCustomizer() {
        return openApi -> {
            if (openApi.getTags() != null) {
                List<Tag> sortedTags = openApi.getTags().stream()
                        .sorted(Comparator.comparingInt(tag -> {
                            int index = TAG_ORDER.indexOf(tag.getName());
                            return index == -1 ? Integer.MAX_VALUE : index;
                        }))
                        .collect(Collectors.toList());
                openApi.setTags(sortedTags);
            }
        };
    }

    // Grouped API for PostgreSQL controllers
    @Bean
    public GroupedOpenApi postgresqlApi() {
        return GroupedOpenApi.builder()
                .group("1. PostgreSQL")
                .displayName("PostgreSQL Database")
                .pathsToMatch("/api/postgresql/**")
                .addOpenApiCustomizer(tagSorterCustomizer())
                .build();
    }

    // Grouped API for MongoDB controllers
    @Bean
    public GroupedOpenApi mongodbApi() {
        return GroupedOpenApi.builder()
                .group("2. MongoDB")
                .displayName("MongoDB Database")
                .pathsToMatch("/api/mongodb/**")
                .addOpenApiCustomizer(tagSorterCustomizer())
                .build();
    }

    // Grouped API for Neo4j controllers
    @Bean
    public GroupedOpenApi neo4jApi() {
        return GroupedOpenApi.builder()
                .group("3. Neo4j")
                .displayName("Neo4j Graph Database")
                .pathsToMatch("/api/neo4j/**")
                .addOpenApiCustomizer(tagSorterCustomizer())
                .build();
    }

    // Grouped API for Data Migration
    @Bean
    public GroupedOpenApi migrationApi() {
        return GroupedOpenApi.builder()
                .group("4. Data Migration")
                .displayName("Data Migration")
                .pathsToMatch("/api/migration/**")
                .addOpenApiCustomizer(tagSorterCustomizer())
                .build();
    }

    // Grouped API for all endpoints
    @Bean
    public GroupedOpenApi allApi() {
        return GroupedOpenApi.builder()
                .group("0. All Endpoints")
                .displayName("All Endpoints")
                .pathsToMatch("/api/**")
                .addOpenApiCustomizer(tagSorterCustomizer())
                .build();
    }
}
