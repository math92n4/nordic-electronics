package com.example.nordicelectronics.controller;

import com.example.nordicelectronics.entity.Payment;
import com.example.nordicelectronics.entity.dto.PaymentRequestDTO;
import com.example.nordicelectronics.entity.dto.PaymentResponseDTO;
import com.example.nordicelectronics.entity.mapper.PaymentMapper;
import com.example.nordicelectronics.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/payments")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @GetMapping("/get-all")
    public List<Payment> getAllPayments() {
        return paymentService.getAllPayments();
    }

    @GetMapping("/by-order")
    public ResponseEntity<PaymentResponseDTO> getPaymentsByOrder(@RequestParam UUID orderId) {
        Payment payment = paymentService.getPaymentByOrderId(orderId);
        PaymentResponseDTO response = PaymentMapper.toResponseDTO(payment);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/create")
    public ResponseEntity<PaymentResponseDTO> createPayment(@RequestBody PaymentRequestDTO paymentDTO) {
        Payment savedPayment = paymentService.createPayment(paymentDTO);
        PaymentResponseDTO response = PaymentMapper.toResponseDTO(savedPayment);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/delete")
    public void deletePaymentById(@RequestParam("paymentId") java.util.UUID paymentId)
    {
        paymentService.deletePaymentById(paymentId);
    }

}
