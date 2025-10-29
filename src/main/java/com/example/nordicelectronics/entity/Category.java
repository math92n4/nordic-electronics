package com.example.nordicelectronics.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.*;

import java.util.UUID;

@Entity
@Data
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
