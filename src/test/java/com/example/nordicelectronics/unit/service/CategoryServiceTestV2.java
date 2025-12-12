package com.example.nordicelectronics.unit.service;



import com.example.nordicelectronics.entity.Category;
import com.example.nordicelectronics.repositories.sql.CategoryRepository;
import com.example.nordicelectronics.service.CategoryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTestV2 {

    @InjectMocks
    private CategoryService categoryService;

    @Mock
    private CategoryRepository categoryRepository;

    @Test
    void getAll_ShouldReturnAList() {
        // Arrange
        Category category1 = new Category();
        Category category2 = new Category();
        List<Category> categories = Arrays.asList(category1, category2);

        when(categoryRepository.findAll()).thenReturn(categories);
        // Act

        List<Category> categoryList = categoryService.getAll();

        // Assert
        assertEquals(2, categoryList.size());
    }

    @Test
    void getAll_ShouldReturnAList_FirstElementsNameIsCorrect() {
        // Arrange
        Category category1 = new Category();
        Category category2 = new Category();
        List<Category> categories = Arrays.asList(category1, category2);

        when(categoryRepository.findAll()).thenReturn(categories);
        // Act

        List<Category> categoryList = categoryService.getAll();

        // Assert
        assertEquals(categories.get(0).getName(), categoryList.get(0).getName());
    }

    @Test
    void getById_ShouldReturnCategory() {
        // Arrange
        Category category1 = new Category();
        category1.setCategoryId(UUID.randomUUID());

        when(categoryRepository.findById(category1.getCategoryId())).thenReturn(Optional.of(category1));
        // Act

        Category categoryResult = categoryService.getById(category1.getCategoryId());

        // Assert
        assertNotNull(categoryResult);
        assertEquals(category1, categoryResult);
    }

    @Test
    void updateCategory_ShouldReturnUpdatedCategory() {
        // Arrange
        UUID categoryId = UUID.randomUUID();

        Category initialCategory = new Category();
        initialCategory.setCategoryId(categoryId);
        initialCategory.setName("Initial Name");
        initialCategory.setDescription("Initial Description");

        Category updatedCategory = new Category();
        updatedCategory.setCategoryId(categoryId);
        updatedCategory.setName("Updated Name");
        updatedCategory.setDescription("Updated Description");

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(initialCategory));
        when(categoryRepository.save(any(Category.class))).thenReturn(updatedCategory);

        // Act
        Category categoryResult = categoryService.update(initialCategory.getCategoryId(), updatedCategory);

        // Assert
        assertEquals(categoryResult.getName(), updatedCategory.getName());
    }

}
