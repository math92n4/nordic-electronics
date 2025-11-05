package com.example.nordicelectronics.repositories.sql;

import com.example.nordicelectronics.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ReviewRepository extends JpaRepository<Review, UUID> {
}
