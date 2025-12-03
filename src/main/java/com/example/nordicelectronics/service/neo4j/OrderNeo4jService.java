package com.example.nordicelectronics.service.neo4j;

import com.example.nordicelectronics.entity.enums.OrderStatus;
import com.example.nordicelectronics.entity.neo4j.OrderNode;
import com.example.nordicelectronics.repositories.neo4j.OrderNeo4jRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderNeo4jService {

    private final OrderNeo4jRepository orderNeo4jRepository;

    public List<OrderNode> getAll() {
        return orderNeo4jRepository.findAll();
    }

    public OrderNode getByOrderId(UUID orderId) {
        return orderNeo4jRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));
    }

    public List<OrderNode> getByUserId(UUID userId) {
        return orderNeo4jRepository.findByUserId(userId);
    }

    public List<OrderNode> getByStatus(OrderStatus status) {
        return orderNeo4jRepository.findByOrderStatus(status);
    }

    public OrderNode save(OrderNode orderNode) {
        if (orderNode.getOrderId() == null) {
            orderNode.setOrderId(UUID.randomUUID());
        }
        return orderNeo4jRepository.save(orderNode);
    }

    public OrderNode update(UUID orderId, OrderNode orderNode) {
        OrderNode existing = getByOrderId(orderId);

        existing.setUserId(orderNode.getUserId());
        existing.setPaymentId(orderNode.getPaymentId());
        existing.setAddressId(orderNode.getAddressId());
        existing.setOrderDate(orderNode.getOrderDate());
        existing.setOrderStatus(orderNode.getOrderStatus());
        existing.setTotalAmount(orderNode.getTotalAmount());
        existing.setSubtotal(orderNode.getSubtotal());
        existing.setTaxAmount(orderNode.getTaxAmount());
        existing.setShippingCost(orderNode.getShippingCost());
        existing.setDiscountAmount(orderNode.getDiscountAmount());
        existing.setCouponId(orderNode.getCouponId());

        return orderNeo4jRepository.save(existing);
    }

    public void deleteByOrderId(UUID orderId) {
        orderNeo4jRepository.deleteByOrderId(orderId);
    }
}

