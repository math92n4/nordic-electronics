package com.example.nordicelectronics.repositories.sql;

import com.example.nordicelectronics.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {

    // TODO: Implement method to get stock quantity from the database view
//    @Query(value = "SELECT total_stock FROM vw_product_stock WHERE product_id = :productId",
//            nativeQuery = true)
//    Integer getStockQuantityFromView(@Param("productId") UUID productId);
}
