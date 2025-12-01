package com.example.nordicelectronics.entity.mapper;

import com.example.nordicelectronics.entity.Order;
import com.example.nordicelectronics.entity.OrderProduct;
import com.example.nordicelectronics.entity.dto.address.AddressDTO;
import com.example.nordicelectronics.entity.dto.order.OrderProductResponseDTO;
import com.example.nordicelectronics.entity.dto.order.OrderResponseDTO;
import com.example.nordicelectronics.entity.dto.product.ProductDTO;
import com.example.nordicelectronics.entity.dto.user.UserDTO;

import java.util.List;
import java.util.stream.Collectors;

public class OrderMapper {

    public static OrderResponseDTO toResponseDTO(Order order) {
        if (order == null) {
            return null;
        }

        OrderResponseDTO.OrderResponseDTOBuilder builder = OrderResponseDTO.builder()
                .orderId(order.getOrderId());

        // Map User to UserDTO
        if (order.getUser() != null) {
            UserDTO userDTO = UserDTO.builder()
                    .userId(order.getUser().getUserId())
                    .firstName(order.getUser().getFirstName())
                    .lastName(order.getUser().getLastName())
                    .email(order.getUser().getEmail())
                    .build();
            builder.user(userDTO);
        }

        // Map Address to AddressDTO
        if (order.getAddress() != null) {
            AddressDTO addressDTO = AddressDTO.builder()
                    .addressId(order.getAddress().getAddressId())
                    .street(order.getAddress().getStreet())
                    .streetNumber(order.getAddress().getStreetNumber())
                    .zip(order.getAddress().getZip())
                    .city(order.getAddress().getCity())
                    .build();
            builder.address(addressDTO);
        }

        // Map OrderStatus enum to String
        if (order.getOrderStatus() != null) {
            builder.status(order.getOrderStatus().name());
        }

        // Map order date and amounts
        builder.orderDate(order.getOrderDate());
        builder.totalAmount(order.getTotalAmount());
        builder.subtotal(order.getSubtotal());
        builder.taxAmount(order.getTaxAmount());
        builder.shippingCost(order.getShippingCost());
        builder.discountAmount(order.getDiscountAmount());

        // Map OrderProducts to List<OrderProductResponseDTO>
        if (order.getOrderProducts() != null && !order.getOrderProducts().isEmpty()) {
            List<OrderProductResponseDTO> orderProductDTOs = order.getOrderProducts().stream()
                    .map(OrderMapper::toOrderProductResponseDTO)
                    .collect(Collectors.toList());
            builder.orderProducts(orderProductDTOs);
        }

        return builder.build();
    }

    private static OrderProductResponseDTO toOrderProductResponseDTO(OrderProduct orderProduct) {
        if (orderProduct == null) {
            return null;
        }

        OrderProductResponseDTO.OrderProductResponseDTOBuilder builder = OrderProductResponseDTO.builder()
                .quantity(orderProduct.getQuantity())
                .unitPrice(orderProduct.getUnitPrice())
                .totalPrice(orderProduct.getTotalPrice());

        // Map Product to ProductDTO
        if (orderProduct.getProduct() != null) {
            ProductDTO productDTO = ProductDTO.builder()
                    .productId(orderProduct.getProduct().getProductId())
                    .name(orderProduct.getProduct().getName())
                    .description(orderProduct.getProduct().getDescription())
                    .price(orderProduct.getProduct().getPrice())
                    .weight(orderProduct.getProduct().getWeight())
                    .warrantyId(orderProduct.getProduct().getWarranty() != null 
                            ? orderProduct.getProduct().getWarranty().getWarrantyId() 
                            : null)
                    .brandId(orderProduct.getProduct().getBrand() != null 
                            ? orderProduct.getProduct().getBrand().getBrandId() 
                            : null)
                    .build();
            builder.product(productDTO);
        }

        return builder.build();
    }
}