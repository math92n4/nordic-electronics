package com.example.nordicelectronics.document;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "orders")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderDocument {
    
    @Id
    private String id;
    
    private String userId;
    
    private LocalDateTime orderDate;
    
    private String status; // pending, confirmed, processing, shipped, delivered, cancelled, returned
    
    private BigDecimal totalAmount;
    
    private BigDecimal subtotal;
    
    private BigDecimal taxAmount;
    
    private BigDecimal shippingCost;
    
    private BigDecimal discountAmount;
    
    @Builder.Default
    private List<OrderProductInfo> products = new ArrayList<>();
    
    @Builder.Default
    private List<String> couponIds = new ArrayList<>();
    
    private PaymentInfo payment;
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class OrderProductInfo {
        private String productId;
        private String productName;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal totalPrice;
    }
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class PaymentInfo {
        private String paymentId;
        private String paymentMethod; // credit_card, paypal, bank, klarna, cash
        private String status; // pending, completed, failed, refunded
        private BigDecimal amount;
        private LocalDateTime paymentDate;
    }
}

