package com.example.nordicelectronics.entity.neo4j;

import lombok.*;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.Relationship;
import org.springframework.data.neo4j.core.support.UUIDStringGenerator;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Node("Product")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ProductNode extends BaseNode {

    @Id
    @GeneratedValue(generatorClass = UUIDStringGenerator.class)
    private String id;

    @Property("productId")
    private UUID productId;

    @Property("sku")
    private String sku;

    @Property("name")
    private String name;

    @Property("description")
    private String description;

    @Property("price")
    private BigDecimal price;

    @Property("weight")
    private BigDecimal weight;

    @Property("warrantyId")
    private UUID warrantyId;

    @Property("brandId")
    private UUID brandId;

    @Relationship(type = "MANUFACTURED_BY", direction = Relationship.Direction.OUTGOING)
    private BrandNode brand;

    @Relationship(type = "BELONGS_TO_CATEGORY", direction = Relationship.Direction.OUTGOING)
    @Builder.Default
    private Set<CategoryNode> categories = new HashSet<>();

    @Relationship(type = "HAS_WARRANTY", direction = Relationship.Direction.OUTGOING)
    private WarrantyNode warranty;

    @Relationship(type = "STORED_IN", direction = Relationship.Direction.OUTGOING)
    @Builder.Default
    private List<WarehouseProductRelationship> warehouseProducts = new ArrayList<>();

    @Relationship(type = "HAS_REVIEW", direction = Relationship.Direction.INCOMING)
    @Builder.Default
    private Set<ReviewNode> reviews = new HashSet<>();
}

