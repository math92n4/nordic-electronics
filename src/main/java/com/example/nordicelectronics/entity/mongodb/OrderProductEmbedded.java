package com.example.nordicelectronics.entity.mongodb;

import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class OrderProductEmbedded implements Serializable {

    private UUID productId;
    
    private String productName;
    private String productSku;
    private String productDescription;
    
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
}
