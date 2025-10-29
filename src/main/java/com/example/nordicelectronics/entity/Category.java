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
public class Category {

    @Id
    @GeneratedValue
    @Column(name = "category_id", updatable = false, nullable = false)
    private UUID brandId;
}
