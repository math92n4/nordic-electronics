package com.example.nordicelectronics.entity.mongodb;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
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
    @Indexed(unique = true)
    private UUID productId;

    @Field("sku")
    @Indexed(unique = true)
    private String sku;

    @Field("name")
    @Indexed
    private String name;

    @Field("description")
    private String description;

    @Field("price")
    private BigDecimal price;

    @Field("weight")
    private BigDecimal weight;

    @Field("brand")
    private BrandEmbedded brand;

    @Field("categories")
    @Builder.Default
    private List<CategoryEmbedded> categories = new ArrayList<>();

    @Field("warranty")
    private WarrantyEmbedded warranty;

    @Field("reviews")
    @Builder.Default
    private List<ReviewEmbedded> reviews = new ArrayList<>();

    @Field("warehouse_stock")
    @Builder.Default
    private List<WarehouseStockEmbedded> warehouseStock = new ArrayList<>();
}
