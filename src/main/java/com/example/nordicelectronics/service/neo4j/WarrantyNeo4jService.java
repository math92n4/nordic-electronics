package com.example.nordicelectronics.service.neo4j;

import com.example.nordicelectronics.entity.neo4j.WarrantyNode;
import com.example.nordicelectronics.repositories.neo4j.WarrantyNeo4jRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WarrantyNeo4jService {

    private final WarrantyNeo4jRepository warrantyNeo4jRepository;

    public List<WarrantyNode> getAll() {
        return warrantyNeo4jRepository.findAll();
    }

    public WarrantyNode getByWarrantyId(UUID warrantyId) {
        return warrantyNeo4jRepository.findByWarrantyId(warrantyId)
                .orElseThrow(() -> new RuntimeException("Warranty not found with ID: " + warrantyId));
    }

    public WarrantyNode getByProductId(UUID productId) {
        return warrantyNeo4jRepository.findByProductId(productId)
                .orElseThrow(() -> new RuntimeException("Warranty not found for product ID: " + productId));
    }

    public WarrantyNode save(WarrantyNode warrantyNode) {
        if (warrantyNode.getWarrantyId() == null) {
            warrantyNode.setWarrantyId(UUID.randomUUID());
        }
        return warrantyNeo4jRepository.save(warrantyNode);
    }

    public WarrantyNode update(UUID warrantyId, WarrantyNode warrantyNode) {
        WarrantyNode existing = getByWarrantyId(warrantyId);

        existing.setStartDate(warrantyNode.getStartDate());
        existing.setEndDate(warrantyNode.getEndDate());
        existing.setDescription(warrantyNode.getDescription());
        existing.setProductId(warrantyNode.getProductId());

        return warrantyNeo4jRepository.save(existing);
    }

    public void deleteByWarrantyId(UUID warrantyId) {
        warrantyNeo4jRepository.deleteByWarrantyId(warrantyId);
    }
}

