package com.example.nordicelectronics.repositories.mongodb;

import com.example.nordicelectronics.entity.enums.OrderStatus;
import com.example.nordicelectronics.entity.mongodb.OrderDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderMongoRepository extends MongoRepository<OrderDocument, String> {
    Optional<OrderDocument> findByOrderId(UUID orderId);
    List<OrderDocument> findByUserId(UUID userId);
    List<OrderDocument> findByOrderStatus(OrderStatus orderStatus);
    void deleteByOrderId(UUID orderId);
}

