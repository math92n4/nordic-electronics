package com.example.nordicelectronics.service;

import com.example.nordicelectronics.entity.Address;
import com.example.nordicelectronics.entity.Warehouse;
import com.example.nordicelectronics.entity.dto.warehouse.WarehouseRequestDTO;
import com.example.nordicelectronics.entity.dto.warehouse.WarehouseResponseDTO;
import com.example.nordicelectronics.entity.mapper.WarehouseMapper;
import com.example.nordicelectronics.repositories.sql.AddressRepository;
import com.example.nordicelectronics.repositories.sql.WarehouseRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WarehouseService {

    private final WarehouseRepository warehouseRepository;
    private final AddressRepository addressRepository;

    public List<WarehouseResponseDTO> getAll() {
        return warehouseRepository.findAll().stream()
                .map(WarehouseMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    public WarehouseResponseDTO getById(UUID id) {
        Warehouse warehouse = warehouseRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Warehouse not found"));
        return WarehouseMapper.toResponseDTO(warehouse);
    }

    public WarehouseResponseDTO save(WarehouseRequestDTO dto) {
        Address address = addressRepository.findById(dto.getAddressId())
                .orElseThrow(() -> new EntityNotFoundException("Address not found with id: " + dto.getAddressId()));

        Warehouse warehouse = WarehouseMapper.toEntity(dto);
        warehouse.setAddress(address);
        
        Warehouse saved = warehouseRepository.save(warehouse);
        return WarehouseMapper.toResponseDTO(saved);
    }

    public WarehouseResponseDTO update(UUID id, WarehouseRequestDTO dto) {
        Warehouse existing = warehouseRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Warehouse not found"));

        existing.setName(dto.getName());
        existing.setPhoneNumber(dto.getPhoneNumber());

        if (dto.getAddressId() != null) {
            Address address = addressRepository.findById(dto.getAddressId())
                    .orElseThrow(() -> new EntityNotFoundException("Address not found with id: " + dto.getAddressId()));
            existing.setAddress(address);
        }

        Warehouse saved = warehouseRepository.save(existing);
        return WarehouseMapper.toResponseDTO(saved);
    }

    public void deleteById(UUID id) {
        warehouseRepository.deleteById(id);
    }

}
