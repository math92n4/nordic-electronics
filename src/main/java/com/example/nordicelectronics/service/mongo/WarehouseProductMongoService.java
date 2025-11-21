package com.example.nordicelectronics.service.mongo;

import com.example.nordicelectronics.document.WarehouseProductDocument;
import com.example.nordicelectronics.repositories.mongodb.WarehouseProductMongoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WarehouseProductMongoService {

    private final WarehouseProductMongoRepository warehouseProductMongoRepository;

    public List<WarehouseProductDocument> getAll() {
        return warehouseProductMongoRepository.findAll();
    }

    public WarehouseProductDocument getById(String id) {
        return warehouseProductMongoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Warehouse product not found with id: " + id));
    }

    public WarehouseProductDocument getByWarehouseAndProduct(String warehouseId, String productId) {
        return warehouseProductMongoRepository.findByWarehouseIdAndProductId(warehouseId, productId)
                .orElseThrow(() -> new RuntimeException(
                        "Warehouse product not found for warehouse: " + warehouseId + " and product: " + productId));
    }

    public List<WarehouseProductDocument> getByWarehouseId(String warehouseId) {
        return warehouseProductMongoRepository.findByWarehouseId(warehouseId);
    }

    public List<WarehouseProductDocument> getByProductId(String productId) {
        return warehouseProductMongoRepository.findByProductId(productId);
    }

    public WarehouseProductDocument save(WarehouseProductDocument warehouseProduct) {
        warehouseProduct.setLastUpdated(LocalDateTime.now());
        return warehouseProductMongoRepository.save(warehouseProduct);
    }

    public WarehouseProductDocument updateStock(String warehouseId, String productId, Integer stockQuantity) {
        WarehouseProductDocument existing = getByWarehouseAndProduct(warehouseId, productId);
        existing.setStockQuantity(stockQuantity);
        existing.setLastUpdated(LocalDateTime.now());
        return warehouseProductMongoRepository.save(existing);
    }

    public void deleteById(String id) {
        warehouseProductMongoRepository.deleteById(id);
    }

    public void deleteByWarehouseAndProduct(String warehouseId, String productId) {
        warehouseProductMongoRepository.deleteByWarehouseIdAndProductId(warehouseId, productId);
    }
}

