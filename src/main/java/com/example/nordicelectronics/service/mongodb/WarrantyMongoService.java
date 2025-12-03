package com.example.nordicelectronics.service.mongodb;

import com.example.nordicelectronics.entity.mongodb.WarrantyDocument;
import com.example.nordicelectronics.repositories.mongodb.WarrantyMongoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@ConditionalOnProperty(name = "spring.data.mongodb.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class WarrantyMongoService {

    private final WarrantyMongoRepository warrantyMongoRepository;

    public List<WarrantyDocument> getAll() {
        return warrantyMongoRepository.findAll();
    }

    public WarrantyDocument getByWarrantyId(UUID warrantyId) {
        return warrantyMongoRepository.findByWarrantyId(warrantyId)
                .orElseThrow(() -> new RuntimeException("Warranty not found with ID: " + warrantyId));
    }

    public WarrantyDocument getByProductId(UUID productId) {
        return warrantyMongoRepository.findByProductId(productId)
                .orElseThrow(() -> new RuntimeException("Warranty not found for product ID: " + productId));
    }

    public WarrantyDocument save(WarrantyDocument warrantyDocument) {
        if (warrantyDocument.getWarrantyId() == null) {
            warrantyDocument.setWarrantyId(UUID.randomUUID());
        }
        return warrantyMongoRepository.save(warrantyDocument);
    }

    public WarrantyDocument update(UUID warrantyId, WarrantyDocument warrantyDocument) {
        WarrantyDocument existing = getByWarrantyId(warrantyId);
        
        existing.setStartDate(warrantyDocument.getStartDate());
        existing.setEndDate(warrantyDocument.getEndDate());
        existing.setDescription(warrantyDocument.getDescription());
        existing.setProductId(warrantyDocument.getProductId());

        return warrantyMongoRepository.save(existing);
    }

    public void deleteByWarrantyId(UUID warrantyId) {
        warrantyMongoRepository.deleteByWarrantyId(warrantyId);
    }
}

