package com.example.nordicelectronics.repositories.neo4j;

import com.example.nordicelectronics.entity.neo4j.UserNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserNeo4jRepository extends Neo4jRepository<UserNode, String> {
    Optional<UserNode> findByUserId(UUID userId);
    Optional<UserNode> findByEmail(String email);
    void deleteByUserId(UUID userId);
}

