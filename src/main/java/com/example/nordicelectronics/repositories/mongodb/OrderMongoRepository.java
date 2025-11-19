package com.example.nordicelectronics.repositories.mongodb;

import com.example.nordicelectronics.document.OrderDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderMongoRepository extends MongoRepository<OrderDocument, String> {
    List<OrderDocument> findByUserId(String userId);
    List<OrderDocument> findByStatus(String status);
}

