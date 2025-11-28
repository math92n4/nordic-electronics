package com.example.nordicelectronics.entity.dto.order;

import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderProductRequestDTO {
    private UUID productId;
    private Integer quantity;
    private UUID warehouseId;
}

