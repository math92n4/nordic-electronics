package com.example.nordicelectronics.repositories.sql;

import com.example.nordicelectronics.entity.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface WarehouseRepository extends JpaRepository<Warehouse, UUID> {
}
