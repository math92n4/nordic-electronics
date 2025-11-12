package com.example.nordicelectronics.service;

import com.example.nordicelectronics.entity.Payment;
import com.example.nordicelectronics.repositories.sql.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PaymentService {

    @Autowired
    public PaymentRepository paymentRepository;

    public Payment createPayment(Payment payment) {
        return paymentRepository.save(payment);
    }

    public void deletePaymentById(java.util.UUID paymentId) {
        paymentRepository.deleteById(paymentId);
    }

    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    public List<Payment> getPaymentsByOrderId(java.util.UUID orderId) {
        return paymentRepository.findByOrderId(orderId);
    }

}
