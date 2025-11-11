package com.example.nordicelectronics.service;

import com.example.nordicelectronics.entity.Brand;
import com.example.nordicelectronics.repositories.sql.BrandRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BrandService {

    private final BrandRepository brandRepository;

    public List<Brand> getAll() {
        return brandRepository.findAll();
    }

    public Brand getById(UUID id) {
        return brandRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Brand not found"));
    }

    public Brand save(Brand brand) {
        return brandRepository.save(brand);
    }

    public Brand update(UUID id, Brand brand) {
        Brand existing = getById(id);

        existing.setName(brand.getName());
        existing.setDescription(brand.getDescription());

        return brandRepository.save(existing);
    }

    public void deleteById(UUID id) {
        brandRepository.deleteById(id);
    }


}
