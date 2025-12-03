package com.example.nordicelectronics.repositories.neo4j;

import com.example.nordicelectronics.entity.neo4j.CouponNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CouponNeo4jRepository extends Neo4jRepository<CouponNode, String> {
    Optional<CouponNode> findByCouponId(UUID couponId);
    Optional<CouponNode> findByCode(String code);
    List<CouponNode> findByIsActive(boolean isActive);
    void deleteByCouponId(UUID couponId);
}

