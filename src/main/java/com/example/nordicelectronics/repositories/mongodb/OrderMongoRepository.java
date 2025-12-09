package com.example.nordicelectronics.repositories.mongodb;

import com.example.nordicelectronics.entity.enums.OrderStatus;
import com.example.nordicelectronics.entity.mongodb.OrderDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderMongoRepository extends MongoRepository<OrderDocument, String> {
    Optional<OrderDocument> findByOrderId(UUID orderId);
    
    @Query("{ 'customer.userId': ?0 }")
    List<OrderDocument> findByCustomerUserId(UUID userId);
    
    @Query("{ 'customer.email': ?0 }")
    List<OrderDocument> findByCustomerEmail(String email);
    
    List<OrderDocument> findByOrderStatus(OrderStatus orderStatus);
    
    @Query("{ 'shipping_address.city': ?0 }")
    List<OrderDocument> findByShippingCity(String city);
    
    @Query("{ 'order_products.productId': ?0 }")
    List<OrderDocument> findByProductId(UUID productId);
    
    void deleteByOrderId(UUID orderId);
}
