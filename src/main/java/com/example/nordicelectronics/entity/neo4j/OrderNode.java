package com.example.nordicelectronics.entity.neo4j;

import com.example.nordicelectronics.entity.enums.OrderStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.Relationship;
import org.springframework.data.neo4j.core.support.UUIDStringGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Node("Order")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class OrderNode extends BaseNode {

    @Id
    @GeneratedValue(generatorClass = UUIDStringGenerator.class)
    private String id;

    @Property("orderId")
    private UUID orderId;

    @Property("userId")
    private UUID userId;

    @Property("paymentId")
    private UUID paymentId;

    @Property("addressId")
    private UUID addressId;

    @Property("orderDate")
    private LocalDateTime orderDate;

    @Property("orderStatus")
    private OrderStatus orderStatus;

    @Property("totalAmount")
    private BigDecimal totalAmount;

    @Property("subtotal")
    private BigDecimal subtotal;

    @Property("taxAmount")
    private BigDecimal taxAmount;

    @Property("shippingCost")
    private BigDecimal shippingCost;

    @Property("discountAmount")
    private BigDecimal discountAmount;

    @Property("couponId")
    private UUID couponId;

    @Relationship(type = "PLACED_BY", direction = Relationship.Direction.OUTGOING)
    @JsonIgnore
    private UserNode user;

    @Relationship(type = "SHIPPED_TO", direction = Relationship.Direction.OUTGOING)
    private AddressNode address;

    @Relationship(type = "USED_COUPON", direction = Relationship.Direction.OUTGOING)
    private CouponNode coupon;

    @Relationship(type = "CONTAINS_PRODUCT", direction = Relationship.Direction.OUTGOING)
    @Builder.Default
    private List<OrderProductRelationship> orderProducts = new ArrayList<>();
}

