package com.example.nordicelectronics.entity.neo4j;

import lombok.*;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.Relationship;
import org.springframework.data.neo4j.core.support.UUIDStringGenerator;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Node("User")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class UserNode extends BaseNode {

    @Id
    @GeneratedValue(generatorClass = UUIDStringGenerator.class)
    private String id;

    @Property("userId")
    private UUID userId;

    @Property("firstName")
    private String firstName;

    @Property("lastName")
    private String lastName;

    @Property("email")
    private String email;

    @Property("phoneNumber")
    private String phoneNumber;

    @Property("dateOfBirth")
    private LocalDate dateOfBirth;

    @Property("password")
    private String password;

    @Property("isAdmin")
    private boolean isAdmin;

    @Relationship(type = "BELONGS_TO", direction = Relationship.Direction.INCOMING)
    @Builder.Default
    private List<AddressNode> addresses = new ArrayList<>();

    @Relationship(type = "PLACED_BY", direction = Relationship.Direction.INCOMING)
    @Builder.Default
    private List<OrderNode> orders = new ArrayList<>();
}

