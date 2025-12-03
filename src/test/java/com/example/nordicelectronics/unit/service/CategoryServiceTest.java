package com.example.nordicelectronics.unit.service;

import com.example.nordicelectronics.entity.Category;
import com.example.nordicelectronics.repositories.sql.CategoryRepository;
import com.example.nordicelectronics.service.CategoryService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    private Category testCategory;
    private UUID testCategoryId;

    @BeforeEach
    void setUp() {
        testCategoryId = UUID.randomUUID();
        testCategory = Category.builder()
            .categoryId(testCategoryId)
            .name("Electronics")
            .description("Electronic devices and gadgets")
            .build();
    }

    // ===== GET ALL TESTS =====

    @Test
    void getAll_shouldReturnListOfCategories() {
        // Arrange
        Category category2 = Category.builder()
            .categoryId(UUID.randomUUID())
            .name("Computers")
            .description("Desktop and laptop computers")
            .build();
        List<Category> categories = Arrays.asList(testCategory, category2);
        when(categoryRepository.findAll()).thenReturn(categories);

        // Act
        List<Category> result = categoryService.getAll();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Electronics", result.get(0).getName());
        assertEquals("Computers", result.get(1).getName());

        verify(categoryRepository).findAll();
    }

    @Test
    void getAll_shouldReturnEmptyList_whenNoCategoriesExist() {
        // Arrange
        when(categoryRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<Category> result = categoryService.getAll();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(categoryRepository).findAll();
    }

    // ===== GET BY ID TESTS =====

    @Test
    void getById_shouldReturnCategory_whenCategoryExists() {
        // Arrange
        when(categoryRepository.findById(testCategoryId)).thenReturn(Optional.of(testCategory));

        // Act
        Category result = categoryService.getById(testCategoryId);

        // Assert
        assertNotNull(result);
        assertEquals(testCategoryId, result.getCategoryId());
        assertEquals("Electronics", result.getName());
        assertEquals("Electronic devices and gadgets", result.getDescription());

        verify(categoryRepository).findById(testCategoryId);
    }

    @Test
    void getById_shouldThrowEntityNotFoundException_whenCategoryNotFound() {
        // Arrange
        UUID nonExistentId = UUID.randomUUID();
        when(categoryRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
            () -> categoryService.getById(nonExistentId));

        assertEquals("Category not found", exception.getMessage());
        verify(categoryRepository).findById(nonExistentId);
    }

    // ===== SAVE TESTS =====

    @Test
    void save_shouldSaveAndReturnCategory() {
        // Arrange
        Category newCategory = Category.builder()
            .name("Smartphones")
            .description("Mobile phones and accessories")
            .build();

        Category savedCategory = Category.builder()
            .categoryId(UUID.randomUUID())
            .name("Smartphones")
            .description("Mobile phones and accessories")
            .build();

        when(categoryRepository.save(newCategory)).thenReturn(savedCategory);

        // Act
        Category result = categoryService.save(newCategory);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getCategoryId());
        assertEquals("Smartphones", result.getName());
        assertEquals("Mobile phones and accessories", result.getDescription());

        verify(categoryRepository).save(newCategory);
    }

    // ===== UPDATE TESTS =====

    @Test
    void update_shouldUpdateAndReturnCategory_whenCategoryExists() {
        // Arrange
        Category updateData = Category.builder()
            .name("Consumer Electronics")
            .description("Updated description for electronics")
            .build();

        Category updatedCategory = Category.builder()
            .categoryId(testCategoryId)
            .name("Consumer Electronics")
            .description("Updated description for electronics")
            .build();

        when(categoryRepository.findById(testCategoryId)).thenReturn(Optional.of(testCategory));
        when(categoryRepository.save(any(Category.class))).thenReturn(updatedCategory);

        // Act
        Category result = categoryService.update(testCategoryId, updateData);

        // Assert
        assertNotNull(result);
        assertEquals(testCategoryId, result.getCategoryId());
        assertEquals("Consumer Electronics", result.getName());
        assertEquals("Updated description for electronics", result.getDescription());

        verify(categoryRepository).findById(testCategoryId);
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void update_shouldThrowEntityNotFoundException_whenCategoryNotFound() {
        // Arrange
        UUID nonExistentId = UUID.randomUUID();
        Category updateData = Category.builder()
            .name("Some Category")
            .description("Some description")
            .build();

        when(categoryRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
            () -> categoryService.update(nonExistentId, updateData));

        assertEquals("Category not found", exception.getMessage());
        verify(categoryRepository).findById(nonExistentId);
        verify(categoryRepository, never()).save(any(Category.class));
    }

    // ===== DELETE BY ID TESTS =====

    @Test
    void deleteById_shouldCallRepositoryDeleteById() {
        // Arrange
        doNothing().when(categoryRepository).deleteById(testCategoryId);

        // Act
        categoryService.deleteById(testCategoryId);

        // Assert
        verify(categoryRepository).deleteById(testCategoryId);
    }
}

