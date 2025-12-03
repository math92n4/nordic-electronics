package com.example.nordicelectronics.service.neo4j;

import com.example.nordicelectronics.entity.neo4j.WarehouseNode;
import com.example.nordicelectronics.repositories.neo4j.WarehouseNeo4jRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WarehouseNeo4jService {

    private final WarehouseNeo4jRepository warehouseNeo4jRepository;

    public List<WarehouseNode> getAll() {
        return warehouseNeo4jRepository.findAll();
    }

    public WarehouseNode getByWarehouseId(UUID warehouseId) {
        return warehouseNeo4jRepository.findByWarehouseId(warehouseId)
                .orElseThrow(() -> new RuntimeException("Warehouse not found with ID: " + warehouseId));
    }

    public WarehouseNode save(WarehouseNode warehouseNode) {
        if (warehouseNode.getWarehouseId() == null) {
            warehouseNode.setWarehouseId(UUID.randomUUID());
        }
        return warehouseNeo4jRepository.save(warehouseNode);
    }

    public WarehouseNode update(UUID warehouseId, WarehouseNode warehouseNode) {
        WarehouseNode existing = getByWarehouseId(warehouseId);

        existing.setName(warehouseNode.getName());
        existing.setPhoneNumber(warehouseNode.getPhoneNumber());
        existing.setAddressId(warehouseNode.getAddressId());

        return warehouseNeo4jRepository.save(existing);
    }

    public void deleteByWarehouseId(UUID warehouseId) {
        warehouseNeo4jRepository.deleteByWarehouseId(warehouseId);
    }
}

