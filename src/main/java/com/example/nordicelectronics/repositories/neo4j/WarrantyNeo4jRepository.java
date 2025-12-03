package com.example.nordicelectronics.repositories.neo4j;

import com.example.nordicelectronics.entity.neo4j.WarrantyNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;

import java.util.Optional;
import java.util.UUID;

public interface WarrantyNeo4jRepository extends Neo4jRepository<WarrantyNode, String> {
    Optional<WarrantyNode> findByWarrantyId(UUID warrantyId);
    Optional<WarrantyNode> findByProductId(UUID productId);
    void deleteByWarrantyId(UUID warrantyId);
}

