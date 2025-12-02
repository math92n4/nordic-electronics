package com.example.nordicelectronics.entity.mongodb;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.UUID;

@Document(collection = "addresses")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@EqualsAndHashCode(callSuper = false)
public class AddressDocument extends BaseDocument {
    
    @Id
    private String id;

    @Field("address_id")
    private UUID addressId;

    @Field("user_id")
    private UUID userId;

    @Field("street")
    private String street;

    @Field("street_number")
    private String streetNumber;

    @Field("zip")
    private String zip;

    @Field("city")
    private String city;
}

