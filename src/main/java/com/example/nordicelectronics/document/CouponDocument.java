package com.example.nordicelectronics.document;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDate;

@Document(collection = "coupons")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CouponDocument {
    
    @Id
    private String id;
    
    private String code;
    
    private String discountType; // percentage, fixed_amount
    
    private BigDecimal discountValue;
    
    private BigDecimal minimumOrderValue;
    
    private LocalDate expiryDate;
    
    private Integer usageLimit;
    
    private Integer timesUsed;
    
    private boolean isActive;
}

