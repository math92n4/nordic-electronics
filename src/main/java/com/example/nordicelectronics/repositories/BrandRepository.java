package com.example.nordicelectronics.repositories;

import com.example.nordicelectronics.entity.Brand;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface BrandRepository extends JpaRepository<Brand, UUID> {
}
