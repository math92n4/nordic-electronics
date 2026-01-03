package com.example.nordicelectronics.entity.neo4j;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.Relationship;
import org.springframework.data.neo4j.core.support.UUIDStringGenerator;

import java.util.UUID;

@Node("Address")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@EqualsAndHashCode(callSuper = false)
public class AddressNode extends BaseNode {

    @Id
    @GeneratedValue(generatorClass = UUIDStringGenerator.class)
    private String id;

    @Property("addressId")
    private UUID addressId;

    @Property("userId")
    private UUID userId;

    @Property("street")
    private String street;

    @Property("streetNumber")
    private String streetNumber;

    @Property("zip")
    private String zip;

    @Property("city")
    private String city;

    @Relationship(type = "BELONGS_TO", direction = Relationship.Direction.OUTGOING)
    @JsonIgnore
    private UserNode user;
}

