package com.example.nordicelectronics.service.mongodb;

import com.example.nordicelectronics.entity.mongodb.CategoryDocument;
import com.example.nordicelectronics.repositories.mongodb.CategoryMongoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@ConditionalOnProperty(name = "spring.data.mongodb.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class CategoryMongoService {

    private final CategoryMongoRepository categoryMongoRepository;

    public List<CategoryDocument> getAll() {
        return categoryMongoRepository.findAll();
    }

    public CategoryDocument getByCategoryId(UUID categoryId) {
        return categoryMongoRepository.findByCategoryId(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found with ID: " + categoryId));
    }

    public CategoryDocument save(CategoryDocument categoryDocument) {
        if (categoryDocument.getCategoryId() == null) {
            categoryDocument.setCategoryId(UUID.randomUUID());
        }
        return categoryMongoRepository.save(categoryDocument);
    }

    public CategoryDocument update(UUID categoryId, CategoryDocument categoryDocument) {
        CategoryDocument existing = getByCategoryId(categoryId);
        
        existing.setName(categoryDocument.getName());
        existing.setDescription(categoryDocument.getDescription());

        return categoryMongoRepository.save(existing);
    }

    public void deleteByCategoryId(UUID categoryId) {
        categoryMongoRepository.deleteByCategoryId(categoryId);
    }
}

