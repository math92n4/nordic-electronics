package com.example.nordicelectronics.entity.neo4j;

import lombok.*;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.Relationship;
import org.springframework.data.neo4j.core.support.UUIDStringGenerator;

import java.util.UUID;

@Node("Review")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
public class ReviewNode extends BaseNode {

    @Id
    @GeneratedValue(generatorClass = UUIDStringGenerator.class)
    private String id;

    @Property("reviewId")
    private UUID reviewId;

    @Property("userId")
    private UUID userId;

    @Property("orderId")
    private UUID orderId;

    @Property("reviewValue")
    private int reviewValue;

    @Property("title")
    private String title;

    @Property("comment")
    private String comment;

    @Property("isVerifiedPurchase")
    private boolean isVerifiedPurchase;

    @Property("productId")
    private UUID productId;

    @Relationship(type = "WRITTEN_BY", direction = Relationship.Direction.OUTGOING)
    private UserNode user;

    @Relationship(type = "HAS_REVIEW", direction = Relationship.Direction.OUTGOING)
    private ProductNode product;
}

