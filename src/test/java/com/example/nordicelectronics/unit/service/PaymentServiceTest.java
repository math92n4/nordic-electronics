package com.example.nordicelectronics.unit.service;

import com.example.nordicelectronics.entity.Order;
import com.example.nordicelectronics.entity.Payment;
import com.example.nordicelectronics.entity.dto.payment.PaymentRequestDTO;
import com.example.nordicelectronics.entity.enums.PaymentMethod;
import com.example.nordicelectronics.entity.enums.PaymentStatus;
import com.example.nordicelectronics.repositories.sql.OrderRepository;
import com.example.nordicelectronics.repositories.sql.PaymentRepository;
import com.example.nordicelectronics.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private PaymentService paymentService;

    private UUID testOrderId;
    private UUID testPaymentId;
    private Order testOrder;
    private Payment testPayment;
    private PaymentRequestDTO testPaymentRequestDTO;
    private LocalDateTime testPaymentDate;

    @BeforeEach
    void setUp() {
        testOrderId = UUID.randomUUID();
        testPaymentId = UUID.randomUUID();
        testPaymentDate = LocalDateTime.of(2025, 12, 2, 10, 30);

        testOrder = Order.builder()
            .orderId(testOrderId)
            .build();

        testPayment = Payment.builder()
            .paymentId(testPaymentId)
            .order(testOrder)
            .paymentMethod(PaymentMethod.credit_card)
            .paymentStatus(PaymentStatus.completed)
            .amount(BigDecimal.valueOf(199.99))
            .paymentDate(testPaymentDate)
            .build();

        testPaymentRequestDTO = PaymentRequestDTO.builder()
            .orderId(testOrderId)
            .paymentMethod("credit_card")
            .paymentStatus("completed")
            .amount(BigDecimal.valueOf(199.99))
            .paymentDate(testPaymentDate)
            .build();
    }

    // ===== CREATE PAYMENT TESTS =====

    @Test
    void createPayment_shouldCreateAndReturnPayment_whenValidRequest() {
        // Arrange
        when(orderRepository.findById(testOrderId)).thenReturn(Optional.of(testOrder));
        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);

        // Act
        Payment result = paymentService.createPayment(testPaymentRequestDTO);

        // Assert
        assertNotNull(result);
        assertEquals(testPaymentId, result.getPaymentId());
        assertEquals(PaymentMethod.credit_card, result.getPaymentMethod());
        assertEquals(PaymentStatus.completed, result.getPaymentStatus());
        assertEquals(BigDecimal.valueOf(199.99), result.getAmount());

        verify(orderRepository).findById(testOrderId);
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    void createPayment_shouldThrowIllegalArgumentException_whenOrderIdIsNull() {
        // Arrange
        PaymentRequestDTO requestWithNullOrderId = PaymentRequestDTO.builder()
            .orderId(null)
            .paymentMethod("credit_card")
            .paymentStatus("completed")
            .amount(BigDecimal.valueOf(100))
            .build();

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> paymentService.createPayment(requestWithNullOrderId));

        assertEquals("Payment request must contain a valid Order ID.", exception.getMessage());
        verify(orderRepository, never()).findById(any());
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void createPayment_shouldThrowIllegalArgumentException_whenOrderNotFound() {
        // Arrange
        when(orderRepository.findById(testOrderId)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> paymentService.createPayment(testPaymentRequestDTO));

        assertTrue(exception.getMessage().contains("Order with ID"));
        assertTrue(exception.getMessage().contains("does not exist"));
        verify(orderRepository).findById(testOrderId);
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void createPayment_shouldThrowIllegalArgumentException_whenInvalidPaymentMethod() {
        // Arrange
        PaymentRequestDTO requestWithInvalidMethod = PaymentRequestDTO.builder()
            .orderId(testOrderId)
            .paymentMethod("invalid_method")
            .paymentStatus("completed")
            .amount(BigDecimal.valueOf(100))
            .build();

        when(orderRepository.findById(testOrderId)).thenReturn(Optional.of(testOrder));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> paymentService.createPayment(requestWithInvalidMethod));

        assertEquals("Invalid payment method: invalid_method", exception.getMessage());
        verify(orderRepository).findById(testOrderId);
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void createPayment_shouldThrowIllegalArgumentException_whenInvalidPaymentStatus() {
        // Arrange
        PaymentRequestDTO requestWithInvalidStatus = PaymentRequestDTO.builder()
            .orderId(testOrderId)
            .paymentMethod("credit_card")
            .paymentStatus("invalid_status")
            .amount(BigDecimal.valueOf(100))
            .build();

        when(orderRepository.findById(testOrderId)).thenReturn(Optional.of(testOrder));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> paymentService.createPayment(requestWithInvalidStatus));

        assertEquals("Invalid payment status: invalid_status", exception.getMessage());
        verify(orderRepository).findById(testOrderId);
        verify(paymentRepository, never()).save(any());
    }

    // ===== DELETE PAYMENT BY ID TESTS =====

    @Test
    void deletePaymentById_shouldSoftDeletePayment() {
        // Arrange
        when(paymentRepository.findById(testPaymentId)).thenReturn(Optional.of(testPayment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);

        // Act
        paymentService.deletePaymentById(testPaymentId);

        // Assert - soft delete should find entity, set deletedAt, and save
        verify(paymentRepository).findById(testPaymentId);
        verify(paymentRepository).save(testPayment);
        assertNotNull(testPayment.getDeletedAt());
    }

    @Test
    void deletePaymentById_shouldThrowIllegalArgumentException_whenPaymentNotFound() {
        // Arrange
        UUID nonExistentId = UUID.randomUUID();
        when(paymentRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> paymentService.deletePaymentById(nonExistentId));

        assertTrue(exception.getMessage().contains("Payment not found"));
        verify(paymentRepository).findById(nonExistentId);
        verify(paymentRepository, never()).save(any());
    }

    // ===== GET ALL PAYMENTS TESTS =====

    @Test
    void getAllPayments_shouldReturnListOfPayments() {
        // Arrange
        Payment payment2 = Payment.builder()
            .paymentId(UUID.randomUUID())
            .paymentMethod(PaymentMethod.paypal)
            .paymentStatus(PaymentStatus.pending)
            .amount(BigDecimal.valueOf(299.99))
            .build();

        List<Payment> payments = Arrays.asList(testPayment, payment2);
        when(paymentRepository.findAll()).thenReturn(payments);

        // Act
        List<Payment> result = paymentService.getAllPayments();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(PaymentMethod.credit_card, result.get(0).getPaymentMethod());
        assertEquals(PaymentMethod.paypal, result.get(1).getPaymentMethod());

        verify(paymentRepository).findAll();
    }

    @Test
    void getAllPayments_shouldReturnEmptyList_whenNoPaymentsExist() {
        // Arrange
        when(paymentRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<Payment> result = paymentService.getAllPayments();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(paymentRepository).findAll();
    }

    // ===== GET PAYMENT BY ORDER ID TESTS =====

    @Test
    void getPaymentByOrderId_shouldReturnPayment_whenExists() {
        // Arrange
        when(paymentRepository.findByOrder_OrderId(testOrderId)).thenReturn(testPayment);

        // Act
        Payment result = paymentService.getPaymentByOrderId(testOrderId);

        // Assert
        assertNotNull(result);
        assertEquals(testPaymentId, result.getPaymentId());
        assertEquals(testOrder, result.getOrder());

        verify(paymentRepository).findByOrder_OrderId(testOrderId);
    }

    @Test
    void getPaymentByOrderId_shouldReturnNull_whenNotExists() {
        // Arrange
        UUID nonExistentOrderId = UUID.randomUUID();
        when(paymentRepository.findByOrder_OrderId(nonExistentOrderId)).thenReturn(null);

        // Act
        Payment result = paymentService.getPaymentByOrderId(nonExistentOrderId);

        // Assert
        assertNull(result);

        verify(paymentRepository).findByOrder_OrderId(nonExistentOrderId);
    }
}

