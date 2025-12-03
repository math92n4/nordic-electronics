package com.example.nordicelectronics.entity.neo4j;

import lombok.*;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.RelationshipProperties;
import org.springframework.data.neo4j.core.schema.TargetNode;

import java.util.UUID;

@RelationshipProperties
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class WarehouseProductRelationship {

    @Id
    @GeneratedValue
    private Long id;

    @Property("warehouseId")
    private UUID warehouseId;

    @Property("productId")
    private UUID productId;

    @Property("stockQuantity")
    private int stockQuantity;

    @TargetNode
    private WarehouseNode warehouse;
}

