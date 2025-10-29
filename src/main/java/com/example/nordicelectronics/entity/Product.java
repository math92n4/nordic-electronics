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
public class Product {

    @Id
    @GeneratedValue
    @Column(name = "product_id", updatable = false, nullable = false)
    private UUID productId;


}
