package com.example.nordicelectronics.controller;

import com.example.nordicelectronics.entity.Payment;
import com.example.nordicelectronics.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/payments")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @GetMapping("/get-all")
    public Object getAllPayments() {
        return paymentService.getAllPayments();
    }

    @GetMapping("/by-order")
    public Object getPaymentsByOrder(java.util.UUID orderId) {
        return paymentService.getPaymentsByOrderId(orderId);
    }

    @PostMapping("/create")
    public Payment createPayment(Payment payment) {
        return paymentService.createPayment(payment);
    }

    @DeleteMapping("/delete")
    public void deletePaymentById(@RequestParam("paymentId") java.util.UUID paymentId)
    {
        paymentService.deletePaymentById(paymentId);
    }

}
