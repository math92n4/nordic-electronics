package com.example.nordicelectronics.entity.dto.address;

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
public class AddressRequestDTO {
    private String street;
    private String streetNumber;
    private String zip;
    private String city;
}

