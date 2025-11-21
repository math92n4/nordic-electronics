package com.example.nordicelectronics.document;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@Document(collection = "users")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDocument {
    
    @Id
    private String id;
    
    private String email;
    
    private String password;
    
    private String firstName;
    
    private String lastName;
    
    private String phoneNumber;
    
    private LocalDate dateOfBirth;
    
    private boolean isAdmin;
    
    // Embedded address
    private AddressInfo address;
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class AddressInfo {
        private String street;
        private String streetNumber;
        private String zip;
        private String city;
    }
}

