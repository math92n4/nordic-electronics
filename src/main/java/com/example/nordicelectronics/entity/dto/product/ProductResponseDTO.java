package com.example.nordicelectronics.entity.dto.product;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductResponseDTO {
    private UUID productId;
    private String sku;
    private String name;
    private String description;
    private BigDecimal price;
    private BigDecimal weight;
    private UUID warrantyId;
    private UUID brandId;
    private List<UUID> categoryIds;
    private List<UUID> reviewIds;
}

