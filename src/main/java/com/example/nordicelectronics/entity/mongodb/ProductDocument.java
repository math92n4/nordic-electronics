package com.example.nordicelectronics.entity.mongodb;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Document(collection = "products")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ProductDocument extends BaseDocument {

    @Id
    private String id;

    @Field("product_id")
    private UUID productId;

    @Field("sku")
    private String sku;

    @Field("name")
    private String name;

    @Field("description")
    private String description;

    @Field("price")
    private BigDecimal price;

    @Field("weight")
    private BigDecimal weight;

    @Field("category_ids")
    @Builder.Default
    private List<UUID> categoryIds = new ArrayList<>();

    @Field("warranty_id")
    private UUID warrantyId;

    @Field("brand_id")
    private UUID brandId;

    @Field("warehouse_products")
    @Builder.Default
    private List<WarehouseProductEmbedded> warehouseProducts = new ArrayList<>();

    @Field("review_ids")
    @Builder.Default
    private List<UUID> reviewIds = new ArrayList<>();
}

