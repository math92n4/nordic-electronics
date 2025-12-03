package com.example.nordicelectronics.entity.neo4j;

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

@Node("Category")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class CategoryNode extends BaseNode {

    @Id
    @GeneratedValue(generatorClass = UUIDStringGenerator.class)
    private String id;

    @Property("categoryId")
    private UUID categoryId;

    @Property("name")
    private String name;

    @Property("description")
    private String description;

    @Relationship(type = "BELONGS_TO_CATEGORY", direction = Relationship.Direction.INCOMING)
    @Builder.Default
    private Set<ProductNode> products = new HashSet<>();
}

