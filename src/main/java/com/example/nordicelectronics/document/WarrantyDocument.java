package com.example.nordicelectronics.document;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@Document(collection = "warranties")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WarrantyDocument {
    
    @Id
    private String id;
    
    private LocalDate startDate;
    
    private LocalDate endDate;
    
    private String description;
}

