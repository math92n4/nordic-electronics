package com.example.nordicelectronics.service.neo4j;

import com.example.nordicelectronics.entity.enums.PaymentStatus;
import com.example.nordicelectronics.entity.neo4j.PaymentNode;
import com.example.nordicelectronics.repositories.neo4j.PaymentNeo4jRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentNeo4jService {

    private final PaymentNeo4jRepository paymentNeo4jRepository;

    public List<PaymentNode> getAll() {
        return paymentNeo4jRepository.findAll();
    }

    public PaymentNode getByPaymentId(UUID paymentId) {
        return paymentNeo4jRepository.findByPaymentId(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found with ID: " + paymentId));
    }

    public PaymentNode getByOrderId(UUID orderId) {
        return paymentNeo4jRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Payment not found for order ID: " + orderId));
    }

    public List<PaymentNode> getByStatus(PaymentStatus status) {
        return paymentNeo4jRepository.findByPaymentStatus(status);
    }

    public PaymentNode save(PaymentNode paymentNode) {
        if (paymentNode.getPaymentId() == null) {
            paymentNode.setPaymentId(UUID.randomUUID());
        }
        return paymentNeo4jRepository.save(paymentNode);
    }

    public PaymentNode update(UUID paymentId, PaymentNode paymentNode) {
        PaymentNode existing = getByPaymentId(paymentId);

        existing.setOrderId(paymentNode.getOrderId());
        existing.setPaymentMethod(paymentNode.getPaymentMethod());
        existing.setPaymentStatus(paymentNode.getPaymentStatus());
        existing.setPaymentDate(paymentNode.getPaymentDate());
        existing.setAmount(paymentNode.getAmount());

        return paymentNeo4jRepository.save(existing);
    }

    public void deleteByPaymentId(UUID paymentId) {
        paymentNeo4jRepository.deleteByPaymentId(paymentId);
    }
}

