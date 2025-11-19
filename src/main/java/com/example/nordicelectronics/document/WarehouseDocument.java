package com.example.nordicelectronics.document;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "warehouses")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WarehouseDocument {
    
    @Id
    private String id;
    
    private String name;

    // Embedded address
    private WarehouseDocument.AddressInfo address;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class AddressInfo {
        private String street;
        private String streetNumber;
        private String zip;
        private String city;
    }
    
    private String phone;
    
    @Builder.Default
    private List<WarehouseProductInfo> products = new ArrayList<>();
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class WarehouseProductInfo {
        private String productId;
        private Integer stockQuantity;
    }
}

