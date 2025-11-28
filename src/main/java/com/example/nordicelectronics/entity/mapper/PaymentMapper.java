package com.example.nordicelectronics.entity.mapper;

import com.example.nordicelectronics.entity.Payment;
import com.example.nordicelectronics.entity.dto.payment.PaymentResponseDTO;

public class PaymentMapper {

    public static PaymentResponseDTO toResponseDTO(Payment payment) {
        if (payment == null) {
            return null;
        }

        PaymentResponseDTO dto = new PaymentResponseDTO();

        dto.setPaymentId(payment.getPaymentId());
        dto.setOrderId(payment.getOrder().getOrderId());
        dto.setPaymentMethod(payment.getPaymentMethod().name());
        dto.setPaymentStatus(payment.getPaymentStatus().name());
        dto.setPaymentDate(payment.getPaymentDate());
        dto.setAmount(payment.getAmount());

        return dto;
    }
}