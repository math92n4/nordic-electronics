package com.example.nordicelectronics.repositories.neo4j;

import com.example.nordicelectronics.entity.neo4j.WarehouseNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;

import java.util.Optional;
import java.util.UUID;

public interface WarehouseNeo4jRepository extends Neo4jRepository<WarehouseNode, String> {
    Optional<WarehouseNode> findByWarehouseId(UUID warehouseId);
    Optional<WarehouseNode> findByName(String name);
    void deleteByWarehouseId(UUID warehouseId);
}

