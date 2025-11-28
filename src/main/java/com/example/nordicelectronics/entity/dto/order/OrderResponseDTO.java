package com.example.nordicelectronics.entity.dto.order;

import com.example.nordicelectronics.entity.dto.address.AddressDTO;
import com.example.nordicelectronics.entity.dto.user.UserDTO;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponseDTO {
    private UUID orderId;
    private UserDTO user;
    private AddressDTO address;
    private String status;
    private LocalDateTime orderDate;
    private BigDecimal totalAmount;
    private BigDecimal subtotal;
    private BigDecimal taxAmount;
    private BigDecimal shippingCost;
    private BigDecimal discountAmount;
    private List<OrderProductResponseDTO> orderProducts;
}

