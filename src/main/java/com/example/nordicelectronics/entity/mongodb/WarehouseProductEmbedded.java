package com.example.nordicelectronics.entity.mongodb;

import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class WarehouseProductEmbedded implements Serializable {

    private UUID warehouseId;
    
    private UUID productId;

    private int stockQuantity;
}

