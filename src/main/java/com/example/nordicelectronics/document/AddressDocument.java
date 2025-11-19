package com.example.nordicelectronics.document;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "addresses")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AddressDocument {
    
    @Id
    private String id;
    
    private String userId;
    
    private String street;
    
    private String streetNumber;
    
    private String zip;
    
    private String city;
}

