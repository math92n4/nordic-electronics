package com.example.nordicelectronics.entity.mongodb;

import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class AddressEmbedded implements Serializable {

    private UUID addressId;
    private String street;
    private String streetNumber;
    private String zip;
    private String city;
}
