package com.example.nordicelectronics.entity.mongodb;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Document(collection = "warehouses")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class WarehouseDocument extends BaseDocument {

    @Id
    private String id;

    @Field("warehouse_id")
    private UUID warehouseId;

    @Field("name")
    private String name;

    @Field("phone_number")
    private String phoneNumber;

    @Field("warehouse_products")
    @Builder.Default
    private List<WarehouseProductEmbedded> warehouseProducts = new ArrayList<>();

    @Field("address_id")
    private UUID addressId;
}

