package com.example.nordicelectronics.service;

import com.example.nordicelectronics.entity.Brand;
import com.example.nordicelectronics.entity.Warranty;
import com.example.nordicelectronics.repositories.sql.WarrantyRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WarrantyService {

    private final WarrantyRepository warrantyRepository;

    public List<Warranty> getAll() {
        return warrantyRepository.findAll();
    }

    public Warranty getById(UUID id) {
        return warrantyRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Brand not found"));
    }

    public Warranty save(Warranty warranty) {
        return warrantyRepository.save(warranty);
    }

    public Warranty update(UUID id, Warranty warranty) {
        Warranty existing = getById(id);

        existing.setStartDate(warranty.getStartDate());
        existing.setEndDate(warranty.getEndDate());
        existing.setDescription(warranty.getDescription());

        return warrantyRepository.save(existing);
    }

    public void deleteById(UUID id) {
        warrantyRepository.deleteById(id);
    }
}
