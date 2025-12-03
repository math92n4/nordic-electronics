package com.example.nordicelectronics.repositories.neo4j;

import com.example.nordicelectronics.entity.neo4j.ProductNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductNeo4jRepository extends Neo4jRepository<ProductNode, String> {
    Optional<ProductNode> findByProductId(UUID productId);
    Optional<ProductNode> findBySku(String sku);
    List<ProductNode> findByBrandId(UUID brandId);
    
    @Query("MATCH (p:Product)-[:BELONGS_TO_CATEGORY]->(c:Category {categoryId: $categoryId}) RETURN p")
    List<ProductNode> findByCategoryId(UUID categoryId);
    
    void deleteByProductId(UUID productId);
}

