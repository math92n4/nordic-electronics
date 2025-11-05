package com.example.nordicelectronics.repositories.sql;

import com.example.nordicelectronics.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {
}
