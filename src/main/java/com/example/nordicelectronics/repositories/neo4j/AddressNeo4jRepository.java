package com.example.nordicelectronics.repositories.neo4j;

import com.example.nordicelectronics.entity.neo4j.AddressNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AddressNeo4jRepository extends Neo4jRepository<AddressNode, String> {
    Optional<AddressNode> findByAddressId(UUID addressId);
    List<AddressNode> findByUserId(UUID userId);
    void deleteByAddressId(UUID addressId);
}

