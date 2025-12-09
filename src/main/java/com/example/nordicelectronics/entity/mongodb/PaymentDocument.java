package com.example.nordicelectronics.entity.mongodb;

import com.example.nordicelectronics.entity.enums.PaymentMethod;
import com.example.nordicelectronics.entity.enums.PaymentStatus;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Document(collection = "payments")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class PaymentDocument extends BaseDocument {
    
    @Id
    private String id;

    @Field("payment_id")
    @Indexed(unique = true)
    private UUID paymentId;

    @Field("order_id")
    @Indexed
    private UUID orderId;

    @Field("payment_method")
    private PaymentMethod paymentMethod;

    @Field("status")
    private PaymentStatus paymentStatus;

    @Field("payment_date")
    private LocalDateTime paymentDate;

    @Field("amount")
    private BigDecimal amount;
}
