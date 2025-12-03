package com.example.nordicelectronics.repositories.neo4j;

import com.example.nordicelectronics.entity.neo4j.CategoryNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;

import java.util.Optional;
import java.util.UUID;

public interface CategoryNeo4jRepository extends Neo4jRepository<CategoryNode, String> {
    Optional<CategoryNode> findByCategoryId(UUID categoryId);
    Optional<CategoryNode> findByName(String name);
    void deleteByCategoryId(UUID categoryId);
}

