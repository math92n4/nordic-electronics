package com.example.nordicelectronics.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Table(name = "product_variants")
public class ProductVariant {

    @Id
    @GeneratedValue
    @Column(name = "variant_id", updatable = false, nullable = false)
    private UUID productVariantId;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(name = "stock_quantity", nullable = false)
    private int stock_quantity;

    @Column(name = "price", nullable = false)
    private BigDecimal price;

    @Column(name = "weight", nullable = false)
    private BigDecimal weight;

    @Column(name = "sku", nullable = false, unique = true)
    private String sku;

    @ManyToOne
    @JoinColumn(name="product_id", nullable = false)
    private Product product;
}
