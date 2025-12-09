package com.example.nordicelectronics.entity.mongodb;

import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class WarehouseStockEmbedded implements Serializable {

    private UUID warehouseId;
    private String warehouseName;
    private int stockQuantity;
}
