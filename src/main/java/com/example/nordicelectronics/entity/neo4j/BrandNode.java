package com.example.nordicelectronics.entity.neo4j;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.Relationship;
import org.springframework.data.neo4j.core.support.UUIDStringGenerator;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Node("Brand")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class BrandNode extends BaseNode {

    @Id
    @GeneratedValue(generatorClass = UUIDStringGenerator.class)
    private String id;

    @Property("brandId")
    private UUID brandId;

    @Property("name")
    private String name;

    @Property("description")
    private String description;

    @Relationship(type = "MANUFACTURED_BY", direction = Relationship.Direction.INCOMING)
    @Builder.Default
    @JsonIgnore
    private Set<ProductNode> products = new HashSet<>();
}

