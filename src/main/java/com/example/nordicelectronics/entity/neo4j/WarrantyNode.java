package com.example.nordicelectronics.entity.neo4j;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.Relationship;
import org.springframework.data.neo4j.core.support.UUIDStringGenerator;

import java.time.LocalDate;
import java.util.UUID;

@Node("Warranty")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class WarrantyNode extends BaseNode {

    @Id
    @GeneratedValue(generatorClass = UUIDStringGenerator.class)
    private String id;

    @Property("warrantyId")
    private UUID warrantyId;

    @Property("startDate")
    private LocalDate startDate;

    @Property("endDate")
    private LocalDate endDate;

    @Property("description")
    private String description;

    @Property("productId")
    private UUID productId;

    @Relationship(type = "HAS_WARRANTY", direction = Relationship.Direction.INCOMING)
    @JsonIgnore
    private ProductNode product;
}

