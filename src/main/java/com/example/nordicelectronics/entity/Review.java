package com.example.nordicelectronics.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Id;

import java.util.UUID;

public class Review {
    @Id
    @Column(name = "review_id", updatable = false, nullable = false)
    private UUID review_id;
}
