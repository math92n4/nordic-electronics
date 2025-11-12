package com.example.nordicelectronics.entity.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponseDTO {

    private UUID paymentId;
    private UUID orderId;

    private String paymentMethod;
    private String paymentStatus;
    private LocalDateTime paymentDate;
    private BigDecimal amount;

}