package com.example.nordicelectronics.entity.neo4j;

import lombok.*;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.Relationship;
import org.springframework.data.neo4j.core.support.UUIDStringGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Node("Warehouse")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class WarehouseNode extends BaseNode {

    @Id
    @GeneratedValue(generatorClass = UUIDStringGenerator.class)
    private String id;

    @Property("warehouseId")
    private UUID warehouseId;

    @Property("name")
    private String name;

    @Property("phoneNumber")
    private String phoneNumber;

    @Property("addressId")
    private UUID addressId;

    @Relationship(type = "LOCATED_AT", direction = Relationship.Direction.OUTGOING)
    private AddressNode address;

    @Relationship(type = "STORED_IN", direction = Relationship.Direction.INCOMING)
    @Builder.Default
    private List<WarehouseProductRelationship> warehouseProducts = new ArrayList<>();
}

