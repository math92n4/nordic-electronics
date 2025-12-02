package com.example.nordicelectronics.repositories.mongodb;

import com.example.nordicelectronics.entity.enums.PaymentStatus;
import com.example.nordicelectronics.entity.mongodb.PaymentDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentMongoRepository extends MongoRepository<PaymentDocument, String> {
    Optional<PaymentDocument> findByPaymentId(UUID paymentId);
    Optional<PaymentDocument> findByOrderId(UUID orderId);
    List<PaymentDocument> findByPaymentStatus(PaymentStatus paymentStatus);
    void deleteByPaymentId(UUID paymentId);
}

