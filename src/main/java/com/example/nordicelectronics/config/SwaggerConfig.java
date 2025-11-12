package com.example.nordicelectronics.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.tags.Tag;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
public class SwaggerConfig {

    // Define the desired order of tags
    private static final List<String> TAG_ORDER = List.of(
            "Authentication Controller",
            "Product Controller",
            "Order Controller",
            "Customer Controller",
            "Coupon Controller",
            "Category Controller",
            "Warehouse Controller",
            "WarehouseProduct Controller",
            "Warranty Controller",
            "Brand Controller",
            "Ping Controller"
    );

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("Nordic Electronics API")
                        .version("1.0")
                        .description("API documentation for Nordic Electronics Webshop"))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName, new SecurityScheme()
                                .name(securitySchemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")))
                .tags(List.of(
                        new Tag().name("Authentication Controller").description("Handles user authentication and registration"),
                        new Tag().name("Product Controller").description("Product management endpoints"),
                        new Tag().name("Order Controller").description("Order management endpoints"),
                        new Tag().name("Customer Controller").description("Customer management endpoints"),
                        new Tag().name("Coupon Controller").description("Coupon management endpoints"),
                        new Tag().name("Category Controller").description("Category management endpoints"),
                        new Tag().name("Warehouse Controller").description("Warehouse management endpoints"),
                        new Tag().name("WarehouseProduct Controller").description("Warehouse product management endpoints"),
                        new Tag().name("Warranty Controller").description("Warranty management endpoints"),
                        new Tag().name("Brand Controller").description("Brand management endpoints"),
                        new Tag().name("Ping Controller").description("Health check endpoints")
                ));
    }

    @Bean
    public OpenApiCustomizer customTagSorter() {
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
}