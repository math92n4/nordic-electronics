package com.example.nordicelectronics.entity.mongodb;

import com.example.nordicelectronics.entity.enums.PaymentMethod;
import com.example.nordicelectronics.entity.enums.PaymentStatus;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class PaymentEmbedded implements Serializable {

    private UUID paymentId;
    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;
    private LocalDateTime paymentDate;
    private BigDecimal amount;
}
