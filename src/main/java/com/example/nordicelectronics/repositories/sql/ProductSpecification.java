package com.example.nordicelectronics.repositories.sql;

import com.example.nordicelectronics.entity.Product;
import com.example.nordicelectronics.entity.Category;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public class ProductSpecification {

    /**
     * Search products by name or description (case-insensitive)
     */
    public static Specification<Product> searchByNameOrDescription(String search) {
        return (root, query, criteriaBuilder) -> {
            if (search == null || search.trim().isEmpty()) {
                return null;
            }
            String searchPattern = "%" + search.toLowerCase().trim() + "%";
            return criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), searchPattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), searchPattern)
            );
        };
    }

    /**
     * Filter products by brand ID
     */
    public static Specification<Product> filterByBrand(UUID brandId) {
        return (root, query, criteriaBuilder) -> {
            if (brandId == null) {
                return null;
            }
            return criteriaBuilder.equal(root.get("brand").get("brandId"), brandId);
        };
    }

    /**
     * Filter products by category ID
     */
    public static Specification<Product> filterByCategory(UUID categoryId) {
        return (root, query, criteriaBuilder) -> {
            if (categoryId == null) {
                return null;
            }
            Join<Product, Category> categoryJoin = root.join("categories", JoinType.INNER);
            return criteriaBuilder.equal(categoryJoin.get("categoryId"), categoryId);
        };
    }

    /**
     * Combine all filters into a single specification
     */
    public static Specification<Product> withFilters(String search, UUID categoryId, UUID brandId) {
        Specification<Product> spec = Specification.where(null);

        if (search != null && !search.trim().isEmpty()) {
            spec = spec.and(searchByNameOrDescription(search));
        }

        if (categoryId != null) {
            spec = spec.and(filterByCategory(categoryId));
        }

        if (brandId != null) {
            spec = spec.and(filterByBrand(brandId));
        }

        return spec;
    }
}
