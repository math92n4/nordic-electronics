package com.example.nordicelectronics.entity.validator.OrderValidator;

import com.example.nordicelectronics.entity.dto.order.OrderProductRequestDTO;
import com.example.nordicelectronics.entity.dto.order.OrderRequestDTO;

public class ValidateOrderQuantity {

    public static void validate(OrderRequestDTO orderRequestDTO) {
        for(OrderProductRequestDTO orderProduct :  orderRequestDTO.getOrderProducts()){
            if(orderProduct.getQuantity() <= 0){
                throw new IllegalArgumentException("Product quantity must be greater than 0");
            } else if(orderProduct.getQuantity() > 50) {
                throw new IllegalArgumentException("Product quantity must be less than or equal to 50");
            }
        }
    }
}
