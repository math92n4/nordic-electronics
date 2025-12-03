package com.example.nordicelectronics.repositories.neo4j;

import com.example.nordicelectronics.entity.enums.PaymentStatus;
import com.example.nordicelectronics.entity.neo4j.PaymentNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentNeo4jRepository extends Neo4jRepository<PaymentNode, String> {
    Optional<PaymentNode> findByPaymentId(UUID paymentId);
    Optional<PaymentNode> findByOrderId(UUID orderId);
    List<PaymentNode> findByPaymentStatus(PaymentStatus paymentStatus);
    void deleteByPaymentId(UUID paymentId);
}

