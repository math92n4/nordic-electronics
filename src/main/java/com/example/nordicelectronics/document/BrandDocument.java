package com.example.nordicelectronics.document;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "brands")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BrandDocument {
    
    @Id
    private String id;
    
    private String name;
    
    private String description;
}

