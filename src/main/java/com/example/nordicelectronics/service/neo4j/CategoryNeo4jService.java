package com.example.nordicelectronics.service.neo4j;

import com.example.nordicelectronics.entity.neo4j.CategoryNode;
import com.example.nordicelectronics.repositories.neo4j.CategoryNeo4jRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoryNeo4jService {

    private final CategoryNeo4jRepository categoryNeo4jRepository;

    public List<CategoryNode> getAll() {
        return categoryNeo4jRepository.findAll();
    }

    public CategoryNode getByCategoryId(UUID categoryId) {
        return categoryNeo4jRepository.findByCategoryId(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found with ID: " + categoryId));
    }

    public CategoryNode save(CategoryNode categoryNode) {
        if (categoryNode.getCategoryId() == null) {
            categoryNode.setCategoryId(UUID.randomUUID());
        }
        return categoryNeo4jRepository.save(categoryNode);
    }

    public CategoryNode update(UUID categoryId, CategoryNode categoryNode) {
        CategoryNode existing = getByCategoryId(categoryId);

        existing.setName(categoryNode.getName());
        existing.setDescription(categoryNode.getDescription());

        return categoryNeo4jRepository.save(existing);
    }

    public void deleteByCategoryId(UUID categoryId) {
        categoryNeo4jRepository.deleteByCategoryId(categoryId);
    }
}

