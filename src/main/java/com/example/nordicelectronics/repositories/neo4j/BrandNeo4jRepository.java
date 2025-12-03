package com.example.nordicelectronics.repositories.neo4j;

import com.example.nordicelectronics.entity.neo4j.BrandNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;

import java.util.Optional;
import java.util.UUID;

public interface BrandNeo4jRepository extends Neo4jRepository<BrandNode, String> {
    Optional<BrandNode> findByBrandId(UUID brandId);
    Optional<BrandNode> findByName(String name);
    void deleteByBrandId(UUID brandId);
}

