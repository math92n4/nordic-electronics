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
    
    private String address;
    
    private String city;
    
    private String postalCode;
    
    private String country;
    
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

