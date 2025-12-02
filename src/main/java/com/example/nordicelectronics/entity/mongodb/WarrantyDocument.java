package com.example.nordicelectronics.entity.mongodb;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDate;
import java.util.UUID;

@Document(collection = "warranties")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class WarrantyDocument extends BaseDocument {

    @Id
    private String id;

    @Field("warranty_id")
    private UUID warrantyId;

    @Field("start_date")
    private LocalDate startDate;

    @Field("end_date")
    private LocalDate endDate;

    @Field("description")
    private String description;

    @Field("product_id")
    private UUID productId;
}

