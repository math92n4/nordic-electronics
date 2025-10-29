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

public class Address {
    @Id
    @GeneratedValue
    @Column(name = "address_id", updatable = false, nullable = false)
    private UUID address_id;

    @OneToOne
    @JoinColumn(name = "user_id")
    @MapsId
    private User user;

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

    @Column(nullable = false)
    private boolean isAdmin;
}