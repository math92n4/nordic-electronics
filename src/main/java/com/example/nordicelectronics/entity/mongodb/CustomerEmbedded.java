package com.example.nordicelectronics.entity.mongodb;

import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class CustomerEmbedded implements Serializable {

    private UUID userId;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
}
