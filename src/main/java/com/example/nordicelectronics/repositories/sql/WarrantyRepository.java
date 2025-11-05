package com.example.nordicelectronics.repositories.sql;

import com.example.nordicelectronics.entity.Warranty;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface WarrantyRepository extends JpaRepository<Warranty, UUID> {
}
