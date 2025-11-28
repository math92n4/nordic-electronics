package com.example.nordicelectronics.entity.dto.payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponseDTO {
    private UUID paymentId;
    private UUID orderId;
    private String paymentMethod;
    private String paymentStatus;
    private LocalDateTime paymentDate;
    private BigDecimal amount;
}

