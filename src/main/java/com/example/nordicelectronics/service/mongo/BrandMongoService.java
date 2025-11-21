package com.example.nordicelectronics.service.mongo;

import com.example.nordicelectronics.document.BrandDocument;
import com.example.nordicelectronics.repositories.mongodb.BrandMongoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BrandMongoService {

    private final BrandMongoRepository brandMongoRepository;

    public List<BrandDocument> getAll() {
        return brandMongoRepository.findAll();
    }

    public BrandDocument getById(String id) {
        return brandMongoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Brand not found with id: " + id));
    }

    public BrandDocument save(BrandDocument brand) {
        return brandMongoRepository.save(brand);
    }

    public BrandDocument update(String id, BrandDocument brand) {
        BrandDocument existing = getById(id);
        existing.setName(brand.getName());
        existing.setDescription(brand.getDescription());
        return brandMongoRepository.save(existing);
    }

    public void deleteById(String id) {
        brandMongoRepository.deleteById(id);
    }
}

