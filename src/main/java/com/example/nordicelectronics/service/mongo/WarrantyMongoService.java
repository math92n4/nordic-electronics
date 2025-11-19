package com.example.nordicelectronics.service.mongo;

import com.example.nordicelectronics.document.WarrantyDocument;
import com.example.nordicelectronics.repositories.mongodb.WarrantyMongoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WarrantyMongoService {

    private final WarrantyMongoRepository warrantyMongoRepository;

    public List<WarrantyDocument> getAll() {
        return warrantyMongoRepository.findAll();
    }

    public WarrantyDocument getById(String id) {
        return warrantyMongoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Warranty not found with id: " + id));
    }

    public WarrantyDocument save(WarrantyDocument warranty) {
        return warrantyMongoRepository.save(warranty);
    }

    public WarrantyDocument update(String id, WarrantyDocument warranty) {
        WarrantyDocument existing = getById(id);
        existing.setStartDate(warranty.getStartDate());
        existing.setEndDate(warranty.getEndDate());
        existing.setDescription(warranty.getDescription());
        return warrantyMongoRepository.save(existing);
    }

    public void deleteById(String id) {
        warrantyMongoRepository.deleteById(id);
    }
}

