package com.example.nordicelectronics.repositories.sql;

import com.example.nordicelectronics.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {
}
