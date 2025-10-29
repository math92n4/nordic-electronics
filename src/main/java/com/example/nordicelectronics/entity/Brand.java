package com.example.nordicelectronics.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder

public class Brand {

    @Id
    @GeneratedValue
    @Column(name = "brand_id", updatable = false, nullable = false)
    private UUID brandId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String description;
}
