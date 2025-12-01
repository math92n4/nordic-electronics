package com.example.nordicelectronics.entity.dto.warehouse;

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
public class WarehouseResponseDTO {
    private UUID warehouseId;
    private String name;
    private String phoneNumber;
    private UUID addressId;
}

