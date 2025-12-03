package com.example.nordicelectronics.service.mongodb;

import com.example.nordicelectronics.entity.enums.PaymentStatus;
import com.example.nordicelectronics.entity.mongodb.PaymentDocument;
import com.example.nordicelectronics.repositories.mongodb.PaymentMongoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@ConditionalOnProperty(name = "spring.data.mongodb.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class PaymentMongoService {

    private final PaymentMongoRepository paymentMongoRepository;

    public List<PaymentDocument> getAll() {
        return paymentMongoRepository.findAll();
    }

    public PaymentDocument getByPaymentId(UUID paymentId) {
        return paymentMongoRepository.findByPaymentId(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found with ID: " + paymentId));
    }

    public PaymentDocument getByOrderId(UUID orderId) {
        return paymentMongoRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Payment not found for order ID: " + orderId));
    }

    public List<PaymentDocument> getByStatus(PaymentStatus status) {
        return paymentMongoRepository.findByPaymentStatus(status);
    }

    public PaymentDocument save(PaymentDocument paymentDocument) {
        if (paymentDocument.getPaymentId() == null) {
            paymentDocument.setPaymentId(UUID.randomUUID());
        }
        return paymentMongoRepository.save(paymentDocument);
    }

    public PaymentDocument update(UUID paymentId, PaymentDocument paymentDocument) {
        PaymentDocument existing = getByPaymentId(paymentId);
        
        existing.setOrderId(paymentDocument.getOrderId());
        existing.setPaymentMethod(paymentDocument.getPaymentMethod());
        existing.setPaymentStatus(paymentDocument.getPaymentStatus());
        existing.setPaymentDate(paymentDocument.getPaymentDate());
        existing.setAmount(paymentDocument.getAmount());

        return paymentMongoRepository.save(existing);
    }

    public void deleteByPaymentId(UUID paymentId) {
        paymentMongoRepository.deleteByPaymentId(paymentId);
    }
}

