package com.example.nordicelectronics.entity;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class WarehouseProductKey implements Serializable {

    private UUID warehouseId;
    private UUID productId;

}
