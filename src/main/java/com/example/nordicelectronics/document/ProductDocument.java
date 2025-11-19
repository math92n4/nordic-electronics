package com.example.nordicelectronics.document;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "products")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductDocument {
    
    @Id
    private String id;
    
    private String sku;
    
    private String name;
    
    private String description;
    
    private BigDecimal price;
    
    private BigDecimal weight;
    
    // Embedded brand info
    private String brandId;
    private String brandName;
    
    // Embedded warranty info
    private String warrantyId;
    private WarrantyInfo warranty;
    
    // Category IDs
    @Builder.Default
    private List<String> categoryIds = new ArrayList<>();
    
    @Builder.Default
    private List<CategoryInfo> categories = new ArrayList<>();
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class WarrantyInfo {
        private String startDate;
        private String endDate;
        private String description;
    }
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class CategoryInfo {
        private String id;
        private String name;
    }
}

