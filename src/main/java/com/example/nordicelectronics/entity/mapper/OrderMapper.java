package com.example.nordicelectronics.entity.mapper;

import com.example.nordicelectronics.entity.Order;
import com.example.nordicelectronics.entity.dto.OrderResponseDTO;

public class OrderMapper {

    public static OrderResponseDTO toResponseDTO(Order order) {
        if (order == null) {
            return null;
        }

        OrderResponseDTO dto = new OrderResponseDTO();

        // Map simple fields
        dto.setOrderId(order.getOrderId());
        dto.setOrderDate(order.getOrderDate());
        dto.setOrderStatus(order.getOrderStatus().name()); // Convert enum to string
        dto.setTotalAmount(order.getTotalAmount());
        dto.setSubtotal(order.getSubtotal());
        dto.setTaxAmount(order.getTaxAmount());
        dto.setShippingCost(order.getShippingCost());
        dto.setDiscountAmount(order.getDiscountAmount());

        // Safely map the User ID
        if (order.getUser() != null) {
            dto.setUserId(order.getUser().getUserId());
        }

        return dto;
    }
}