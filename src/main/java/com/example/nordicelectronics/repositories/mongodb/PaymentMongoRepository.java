package com.example.nordicelectronics.repositories.mongodb;

import com.example.nordicelectronics.document.PaymentDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentMongoRepository extends MongoRepository<PaymentDocument, String> {
    Optional<PaymentDocument> findByOrderId(String orderId);
    List<PaymentDocument> findByStatus(String status);
}

