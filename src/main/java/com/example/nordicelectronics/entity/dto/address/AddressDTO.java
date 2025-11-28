package com.example.nordicelectronics.entity.dto.address;

import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressDTO {
    private UUID addressId;
    private String street;
    private String streetNumber;
    private String zip;
    private String city;
}

