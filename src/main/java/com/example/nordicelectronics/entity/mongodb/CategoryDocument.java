package com.example.nordicelectronics.entity.mongodb;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Document(collection = "categories")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class CategoryDocument extends BaseDocument {

    @Id
    private String id;

    @Field("category_id")
    private UUID categoryId;

    @Field("name")
    private String name;

    @Field("description")
    private String description;

    @Field("product_ids")
    @Builder.Default
    private List<UUID> productIds = new ArrayList<>();
}

