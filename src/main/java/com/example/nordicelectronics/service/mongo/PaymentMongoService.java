package com.example.nordicelectronics.service.mongo;

import com.example.nordicelectronics.document.PaymentDocument;
import com.example.nordicelectronics.repositories.mongodb.PaymentMongoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentMongoService {

    private final PaymentMongoRepository paymentMongoRepository;

    public List<PaymentDocument> getAll() {
        return paymentMongoRepository.findAll();
    }

    public PaymentDocument getById(String id) {
        return paymentMongoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found with id: " + id));
    }

    public PaymentDocument getByOrderId(String orderId) {
        return paymentMongoRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Payment not found for order: " + orderId));
    }

    public List<PaymentDocument> getByStatus(String status) {
        return paymentMongoRepository.findByStatus(status);
    }

    public PaymentDocument save(PaymentDocument payment) {
        return paymentMongoRepository.save(payment);
    }

    public PaymentDocument update(String id, PaymentDocument payment) {
        PaymentDocument existing = getById(id);
        existing.setPaymentMethod(payment.getPaymentMethod());
        existing.setAmount(payment.getAmount());
        existing.setStatus(payment.getStatus());
        existing.setPaymentDate(payment.getPaymentDate());
        return paymentMongoRepository.save(existing);
    }

    public void deleteById(String id) {
        paymentMongoRepository.deleteById(id);
    }
}

