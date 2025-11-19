package com.example.nordicelectronics.document;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "warehouse_products")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WarehouseProductDocument {
    
    @Id
    private String id;
    
    private String warehouseId;
    
    private String warehouseName;
    
    private String productId;
    
    private String productName;
    
    private String productSku;
    
    private Integer stockQuantity;
    
    private LocalDateTime lastUpdated;
}

