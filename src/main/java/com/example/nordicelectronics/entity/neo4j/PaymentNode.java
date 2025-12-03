package com.example.nordicelectronics.entity.neo4j;

import com.example.nordicelectronics.entity.enums.PaymentMethod;
import com.example.nordicelectronics.entity.enums.PaymentStatus;
import lombok.*;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.Relationship;
import org.springframework.data.neo4j.core.support.UUIDStringGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Node("Payment")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class PaymentNode extends BaseNode {

    @Id
    @GeneratedValue(generatorClass = UUIDStringGenerator.class)
    private String id;

    @Property("paymentId")
    private UUID paymentId;

    @Property("orderId")
    private UUID orderId;

    @Property("paymentMethod")
    private PaymentMethod paymentMethod;

    @Property("paymentStatus")
    private PaymentStatus paymentStatus;

    @Property("paymentDate")
    private LocalDateTime paymentDate;

    @Property("amount")
    private BigDecimal amount;

    @Relationship(type = "PAYMENT_FOR", direction = Relationship.Direction.OUTGOING)
    private OrderNode order;
}

