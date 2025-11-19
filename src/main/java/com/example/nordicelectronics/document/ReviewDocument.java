package com.example.nordicelectronics.document;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "reviews")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReviewDocument {
    
    @Id
    private String id;
    
    private String productId;
    
    private String userId;
    
    private String orderId;
    
    private Integer reviewValue; // 1-5
    
    private String title;
    
    private String comment;
    
    private boolean isVerifiedPurchase;
    
    private LocalDateTime createdAt;
}

