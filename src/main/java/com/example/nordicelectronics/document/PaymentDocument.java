package com.example.nordicelectronics.document;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Document(collection = "payments")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentDocument {
    
    @Id
    private String id;
    
    private String orderId;
    
    private String paymentMethod; // credit_card, paypal, bank, klarna, cash
    
    private BigDecimal amount;
    
    private String status; // pending, completed, failed, refunded
    
    private LocalDateTime paymentDate;
}

