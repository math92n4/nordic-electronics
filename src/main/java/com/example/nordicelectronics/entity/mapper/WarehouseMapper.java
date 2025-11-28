package com.example.nordicelectronics.entity.mapper;

import com.example.nordicelectronics.entity.Warehouse;
import com.example.nordicelectronics.entity.dto.warehouse.WarehouseRequestDTO;
import com.example.nordicelectronics.entity.dto.warehouse.WarehouseResponseDTO;

public class WarehouseMapper {

    public static WarehouseResponseDTO toResponseDTO(Warehouse warehouse) {
        if (warehouse == null) {
            return null;
        }

        return WarehouseResponseDTO.builder()
                .warehouseId(warehouse.getWarehouseId())
                .name(warehouse.getName())
                .phoneNumber(warehouse.getPhoneNumber())
                .addressId(warehouse.getAddress() != null ? warehouse.getAddress().getAddressId() : null)
                .build();
    }

    public static Warehouse toEntity(WarehouseRequestDTO dto) {
        if (dto == null) {
            return null;
        }

        return Warehouse.builder()
                .name(dto.getName())
                .phoneNumber(dto.getPhoneNumber())
                .build();
    }
}

