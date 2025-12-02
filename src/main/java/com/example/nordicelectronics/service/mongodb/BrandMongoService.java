package com.example.nordicelectronics.service.mongodb;

import com.example.nordicelectronics.entity.mongodb.BrandDocument;
import com.example.nordicelectronics.repositories.mongodb.BrandMongoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BrandMongoService {

    private final BrandMongoRepository brandMongoRepository;

    public List<BrandDocument> getAll() {
        return brandMongoRepository.findAll();
    }

    public BrandDocument getByBrandId(UUID brandId) {
        return brandMongoRepository.findByBrandId(brandId)
                .orElseThrow(() -> new RuntimeException("Brand not found with ID: " + brandId));
    }

    public BrandDocument save(BrandDocument brandDocument) {
        if (brandDocument.getBrandId() == null) {
            brandDocument.setBrandId(UUID.randomUUID());
        }
        return brandMongoRepository.save(brandDocument);
    }

    public BrandDocument update(UUID brandId, BrandDocument brandDocument) {
        BrandDocument existing = getByBrandId(brandId);
        
        existing.setName(brandDocument.getName());
        existing.setDescription(brandDocument.getDescription());

        return brandMongoRepository.save(existing);
    }

    public void deleteByBrandId(UUID brandId) {
        brandMongoRepository.deleteByBrandId(brandId);
    }
}

