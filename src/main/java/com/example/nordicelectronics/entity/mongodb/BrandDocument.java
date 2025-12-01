package com.example.nordicelectronics.entity.mongodb;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Document(collection = "brands")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class BrandDocument extends BaseDocument {

    @Id
    private String id;

    @Field("brand_id")
    private UUID brandId;

    @Field("name")
    private String name;

    @Field("description")
    private String description;

    @Field("product_ids")
    @Builder.Default
    private List<UUID> productIds = new ArrayList<>();
}

