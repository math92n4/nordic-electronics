package com.example.nordicelectronics.repositories.sql;

import com.example.nordicelectronics.entity.WarehouseProduct;
import com.example.nordicelectronics.entity.WarehouseProductKey;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WarehouseProductRepository extends JpaRepository<WarehouseProduct, WarehouseProductKey> {
}
