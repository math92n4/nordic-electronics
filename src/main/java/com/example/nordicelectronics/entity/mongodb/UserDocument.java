package com.example.nordicelectronics.entity.mongodb;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Document(collection = "users")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class UserDocument extends BaseDocument {
    
    @Id
    private String id;

    @Field("user_id")
    @Indexed(unique = true)
    private UUID userId;

    @Field("first_name")
    private String firstName;

    @Field("last_name")
    private String lastName;

    @Field("email")
    @Indexed(unique = true)
    private String email;

    @Field("phone_number")
    private String phoneNumber;

    @Field("date_of_birth")
    private LocalDate dateOfBirth;

    @Field("password")
    private String password;

    @Field("is_admin")
    private boolean isAdmin;

    // EMBEDDED: Addresses belong to this user (1:few relationship)
    @Field("addresses")
    @Builder.Default
    private List<AddressEmbedded> addresses = new ArrayList<>();

    // References only - orders are large and accessed separately
    @Field("order_ids")
    @Builder.Default
    private List<UUID> orderIds = new ArrayList<>();
}
