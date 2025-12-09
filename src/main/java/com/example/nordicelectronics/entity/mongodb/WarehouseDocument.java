package com.example.nordicelectronics.entity.mongodb;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
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
    @Indexed(unique = true)
    private UUID warehouseId;

    @Field("name")
    private String name;

    @Field("phone_number")
    private String phoneNumber;

    @Field("address")
    private AddressEmbedded address;

    @Field("products")
    @Builder.Default
    private List<WarehouseProductEmbedded> products = new ArrayList<>();
}
