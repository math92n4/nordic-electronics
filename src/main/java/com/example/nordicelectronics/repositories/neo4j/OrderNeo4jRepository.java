package com.example.nordicelectronics.repositories.neo4j;

import com.example.nordicelectronics.entity.enums.OrderStatus;
import com.example.nordicelectronics.entity.neo4j.OrderNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderNeo4jRepository extends Neo4jRepository<OrderNode, String> {
    Optional<OrderNode> findByOrderId(UUID orderId);
    List<OrderNode> findByUserId(UUID userId);
    List<OrderNode> findByOrderStatus(OrderStatus orderStatus);
    void deleteByOrderId(UUID orderId);
}

