package com.example.nordicelectronics.service.neo4j;

import com.example.nordicelectronics.entity.neo4j.BrandNode;
import com.example.nordicelectronics.repositories.neo4j.BrandNeo4jRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BrandNeo4jService {

    private final BrandNeo4jRepository brandNeo4jRepository;

    public List<BrandNode> getAll() {
        return brandNeo4jRepository.findAll();
    }

    public BrandNode getByBrandId(UUID brandId) {
        return brandNeo4jRepository.findByBrandId(brandId)
                .orElseThrow(() -> new RuntimeException("Brand not found with ID: " + brandId));
    }

    public BrandNode save(BrandNode brandNode) {
        if (brandNode.getBrandId() == null) {
            brandNode.setBrandId(UUID.randomUUID());
        }
        return brandNeo4jRepository.save(brandNode);
    }

    public BrandNode update(UUID brandId, BrandNode brandNode) {
        BrandNode existing = getByBrandId(brandId);

        existing.setName(brandNode.getName());
        existing.setDescription(brandNode.getDescription());

        return brandNeo4jRepository.save(existing);
    }

    public void deleteByBrandId(UUID brandId) {
        brandNeo4jRepository.deleteByBrandId(brandId);
    }
}

