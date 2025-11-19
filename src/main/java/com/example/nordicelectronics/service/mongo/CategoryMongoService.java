package com.example.nordicelectronics.service.mongo;

import com.example.nordicelectronics.document.CategoryDocument;
import com.example.nordicelectronics.repositories.mongodb.CategoryMongoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryMongoService {

    private final CategoryMongoRepository categoryMongoRepository;

    public List<CategoryDocument> getAll() {
        return categoryMongoRepository.findAll();
    }

    public CategoryDocument getById(String id) {
        return categoryMongoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));
    }

    public CategoryDocument save(CategoryDocument category) {
        return categoryMongoRepository.save(category);
    }

    public CategoryDocument update(String id, CategoryDocument category) {
        CategoryDocument existing = getById(id);
        existing.setName(category.getName());
        existing.setDescription(category.getDescription());
        return categoryMongoRepository.save(existing);
    }

    public void deleteById(String id) {
        categoryMongoRepository.deleteById(id);
    }
}

