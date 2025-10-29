package com.example.nordicelectronics.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder

public class User {
    @Id
    @GeneratedValue
    @Column(name = "user_id", updatable = false, nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String phoneNumber;

    @Column(nullable = false)
    private String dateOfBirth;

    @Column(nullable = false)
    private String password;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    @PrimaryKeyJoinColumn
    private Address address;

    @Column(nullable = false)
    private boolean isAdmin;
}