package com.example.nordicelectronics.service.mongo;

import com.example.nordicelectronics.document.OrderDocument;
import com.example.nordicelectronics.repositories.mongodb.OrderMongoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderMongoService {

    private final OrderMongoRepository orderMongoRepository;

    public List<OrderDocument> getAll() {
        return orderMongoRepository.findAll();
    }

    public OrderDocument getById(String id) {
        return orderMongoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));
    }

    public OrderDocument save(OrderDocument order) {
        return orderMongoRepository.save(order);
    }

    public OrderDocument update(String id, OrderDocument order) {
        OrderDocument existing = getById(id);
        existing.setStatus(order.getStatus());
        existing.setTotalAmount(order.getTotalAmount());
        existing.setSubtotal(order.getSubtotal());
        existing.setTaxAmount(order.getTaxAmount());
        existing.setShippingCost(order.getShippingCost());
        existing.setDiscountAmount(order.getDiscountAmount());
        
        if (order.getProducts() != null) {
            existing.setProducts(order.getProducts());
        }
        
        if (order.getCouponIds() != null) {
            existing.setCouponIds(order.getCouponIds());
        }
        
        if (order.getPayment() != null) {
            existing.setPayment(order.getPayment());
        }
        
        return orderMongoRepository.save(existing);
    }

    public void deleteById(String id) {
        orderMongoRepository.deleteById(id);
    }

    public List<OrderDocument> getByUserId(String userId) {
        return orderMongoRepository.findByUserId(userId);
    }

    public List<OrderDocument> getByStatus(String status) {
        return orderMongoRepository.findByStatus(status);
    }
}

