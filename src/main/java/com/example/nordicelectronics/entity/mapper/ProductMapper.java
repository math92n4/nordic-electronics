package com.example.nordicelectronics.entity.mapper;

import com.example.nordicelectronics.entity.Product;
import com.example.nordicelectronics.entity.dto.product.ProductRequestDTO;
import com.example.nordicelectronics.entity.dto.product.ProductResponseDTO;

import java.util.List;
import java.util.stream.Collectors;

public class ProductMapper {

    public static ProductResponseDTO toResponseDTO(Product product) {
        if (product == null) {
            return null;
        }

        List<java.util.UUID> categoryIds = product.getCategories() != null
                ? product.getCategories().stream()
                    .map(category -> category.getCategoryId())
                    .collect(Collectors.toList())
                : null;

        List<java.util.UUID> reviewIds = product.getReviews() != null
                ? product.getReviews().stream()
                    .map(review -> review.getReviewId())
                    .collect(Collectors.toList())
                : null;

        return ProductResponseDTO.builder()
                .productId(product.getProductId())
                .sku(product.getSku())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .weight(product.getWeight())
                .warrantyId(product.getWarranty() != null ? product.getWarranty().getWarrantyId() : null)
                .brandId(product.getBrand() != null ? product.getBrand().getBrandId() : null)
                .categoryIds(categoryIds)
                .reviewIds(reviewIds)
                .build();
    }

    public static Product toEntity(ProductRequestDTO dto) {
        if (dto == null) {
            return null;
        }

        return Product.builder()
                .sku(dto.getSku())
                .name(dto.getName())
                .description(dto.getDescription())
                .price(dto.getPrice())
                .weight(dto.getWeight())
                .build();
    }
}

