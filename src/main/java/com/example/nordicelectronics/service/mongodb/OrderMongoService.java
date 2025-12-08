package com.example.nordicelectronics.service.mongodb;

import com.example.nordicelectronics.entity.enums.OrderStatus;
import com.example.nordicelectronics.entity.mongodb.OrderDocument;
import com.example.nordicelectronics.repositories.mongodb.OrderMongoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@ConditionalOnProperty(name = "spring.data.mongodb.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class OrderMongoService {

    private final OrderMongoRepository orderMongoRepository;

    public List<OrderDocument> getAll() {
        return orderMongoRepository.findAll();
    }

    public OrderDocument getByOrderId(UUID orderId) {
        return orderMongoRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));
    }

    public List<OrderDocument> getByUserId(UUID userId) {
        return orderMongoRepository.findByCustomerUserId(userId);
    }

    public List<OrderDocument> getByStatus(OrderStatus status) {
        return orderMongoRepository.findByOrderStatus(status);
    }

    public OrderDocument save(OrderDocument orderDocument) {
        if (orderDocument.getOrderId() == null) {
            orderDocument.setOrderId(UUID.randomUUID());
        }
        return orderMongoRepository.save(orderDocument);
    }

    public OrderDocument update(UUID orderId, OrderDocument orderDocument) {
        OrderDocument existing = getByOrderId(orderId);
        
        existing.setCustomer(orderDocument.getCustomer());
        existing.setShippingAddress(orderDocument.getShippingAddress());
        existing.setPayment(orderDocument.getPayment());
        existing.setOrderDate(orderDocument.getOrderDate());
        existing.setOrderStatus(orderDocument.getOrderStatus());
        existing.setTotalAmount(orderDocument.getTotalAmount());
        existing.setSubtotal(orderDocument.getSubtotal());
        existing.setTaxAmount(orderDocument.getTaxAmount());
        existing.setShippingCost(orderDocument.getShippingCost());
        existing.setDiscountAmount(orderDocument.getDiscountAmount());
        existing.setOrderProducts(orderDocument.getOrderProducts());
        existing.setCoupon(orderDocument.getCoupon());

        return orderMongoRepository.save(existing);
    }

    public void deleteByOrderId(UUID orderId) {
        orderMongoRepository.deleteByOrderId(orderId);
    }
}
