package com.example.nordicelectronics.service;

import com.example.nordicelectronics.entity.Order;
import com.example.nordicelectronics.entity.Payment;
import com.example.nordicelectronics.entity.dto.payment.PaymentRequestDTO;
import com.example.nordicelectronics.entity.enums.PaymentMethod;
import com.example.nordicelectronics.entity.enums.PaymentStatus;
import com.example.nordicelectronics.repositories.sql.OrderRepository;
import com.example.nordicelectronics.repositories.sql.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class PaymentService {

    @Autowired
    public PaymentRepository paymentRepository;

    @Autowired
    public OrderRepository orderRepository;

    public Payment createPayment(PaymentRequestDTO paymentDTO) {
        UUID orderId = paymentDTO.getOrderId();

        if (orderId == null) {
            throw new IllegalArgumentException("Payment request must contain a valid Order ID.");
        }

        Order existingOrder = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order with ID " + orderId + " does not exist."));

        Payment newPayment = new Payment();
        newPayment.setOrder(existingOrder);

        String methodString = paymentDTO.getPaymentMethod();

        try {
            PaymentMethod methodEnum = PaymentMethod.valueOf(methodString);
            newPayment.setPaymentMethod(methodEnum);

        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid payment method: " + methodString);
        }

        String statusString = paymentDTO.getPaymentStatus();
        try {
            PaymentStatus statusEnum = PaymentStatus.valueOf(statusString);
            newPayment.setPaymentStatus(statusEnum);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid payment status: " + statusString);
        }
        newPayment.setAmount(paymentDTO.getAmount());
        newPayment.setPaymentDate(paymentDTO.getPaymentDate());

        return paymentRepository.save(newPayment);
    }

    public void deletePaymentById(java.util.UUID paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found with id: " + paymentId));
        payment.softDelete();
        paymentRepository.save(payment);
    }

    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    public Payment getPaymentByOrderId(java.util.UUID orderId) {
        return paymentRepository.findByOrder_OrderId(orderId);
    }

}
