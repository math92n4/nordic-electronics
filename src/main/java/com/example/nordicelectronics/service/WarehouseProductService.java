package com.example.nordicelectronics.service;

import com.example.nordicelectronics.entity.Product;
import com.example.nordicelectronics.entity.Warehouse;
import com.example.nordicelectronics.entity.WarehouseProduct;
import com.example.nordicelectronics.entity.WarehouseProductKey;
import com.example.nordicelectronics.repositories.sql.ProductRepository;
import com.example.nordicelectronics.repositories.sql.WarehouseProductRepository;
import com.example.nordicelectronics.repositories.sql.WarehouseRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WarehouseProductService {

    private final WarehouseProductRepository warehouseProductRepository;
    private final WarehouseRepository warehouseRepository;
    private final ProductRepository productRepository;


    public List<WarehouseProduct> getAll() {
        return warehouseProductRepository.findAll();
    }

    public WarehouseProduct getById(UUID warehouseId, UUID productId) {
        WarehouseProductKey key = new WarehouseProductKey();
        key.setWarehouseId(warehouseId);
        key.setProductId(productId);
        return warehouseProductRepository.findById(key)
                .orElseThrow(() -> new EntityNotFoundException(
                        "WarehouseProduct not found for warehouseId=" + warehouseId + ", productId=" + productId));
    }

    public WarehouseProduct save(UUID warehouseId, UUID productId, int stockQuantity) {
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new EntityNotFoundException("Warehouse not found"));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));

        WarehouseProductKey key = new WarehouseProductKey();
        key.setWarehouseId(warehouseId);
        key.setProductId(productId);

        WarehouseProduct warehouseProduct = WarehouseProduct.builder()
                .id(key)
                .warehouse(warehouse)
                .product(product)
                .stockQuantity(stockQuantity)
                .build();

        return warehouseProductRepository.save(warehouseProduct);
    }

    public WarehouseProduct updateStock(UUID warehouseId, UUID productId, int newStock) {
        WarehouseProduct existing = getById(warehouseId, productId);
        existing.setStockQuantity(newStock);
        return warehouseProductRepository.save(existing);
    }

    public void deleteById(UUID warehouseId, UUID productId) {
        warehouseProductRepository.deleteById(new WarehouseProductKey(warehouseId, productId));
    }
}
