package com.example.nordicelectronics.entity.dto.warehouse_product;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class WarehouseProductDTO {

    private UUID warehouseId;
    private UUID productId;
    private int stockQuantity;
}
