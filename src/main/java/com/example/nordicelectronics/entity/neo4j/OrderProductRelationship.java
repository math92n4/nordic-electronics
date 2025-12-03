package com.example.nordicelectronics.entity.neo4j;

import lombok.*;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.RelationshipProperties;
import org.springframework.data.neo4j.core.schema.TargetNode;

import java.math.BigDecimal;
import java.util.UUID;

@RelationshipProperties
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class OrderProductRelationship {

    @Id
    @GeneratedValue
    private Long id;

    @Property("orderId")
    private UUID orderId;

    @Property("productId")
    private UUID productId;

    @Property("quantity")
    private Integer quantity;

    @Property("unitPrice")
    private BigDecimal unitPrice;

    @Property("totalPrice")
    private BigDecimal totalPrice;

    @TargetNode
    private ProductNode product;
}

