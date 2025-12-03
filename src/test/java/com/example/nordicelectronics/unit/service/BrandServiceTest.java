package com.example.nordicelectronics.unit.service;

import com.example.nordicelectronics.entity.Brand;
import com.example.nordicelectronics.repositories.sql.BrandRepository;
import com.example.nordicelectronics.service.BrandService;
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
class BrandServiceTest {

    @Mock
    private BrandRepository brandRepository;

    @InjectMocks
    private BrandService brandService;

    private Brand testBrand;
    private UUID testBrandId;

    @BeforeEach
    void setUp() {
        testBrandId = UUID.randomUUID();
        testBrand = Brand.builder()
            .brandId(testBrandId)
            .name("Samsung")
            .description("Korean electronics manufacturer")
            .build();
    }

    // ===== GET ALL TESTS =====

    @Test
    void getAll_shouldReturnListOfBrands() {
        // Arrange
        Brand brand2 = Brand.builder()
            .brandId(UUID.randomUUID())
            .name("Apple")
            .description("American technology company")
            .build();
        List<Brand> brands = Arrays.asList(testBrand, brand2);
        when(brandRepository.findAll()).thenReturn(brands);

        // Act
        List<Brand> result = brandService.getAll();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Samsung", result.get(0).getName());
        assertEquals("Apple", result.get(1).getName());

        verify(brandRepository).findAll();
    }

    @Test
    void getAll_shouldReturnEmptyList_whenNoBrandsExist() {
        // Arrange
        when(brandRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<Brand> result = brandService.getAll();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(brandRepository).findAll();
    }

    // ===== GET BY ID TESTS =====

    @Test
    void getById_shouldReturnBrand_whenBrandExists() {
        // Arrange
        when(brandRepository.findById(testBrandId)).thenReturn(Optional.of(testBrand));

        // Act
        Brand result = brandService.getById(testBrandId);

        // Assert
        assertNotNull(result);
        assertEquals(testBrandId, result.getBrandId());
        assertEquals("Samsung", result.getName());
        assertEquals("Korean electronics manufacturer", result.getDescription());

        verify(brandRepository).findById(testBrandId);
    }

    @Test
    void getById_shouldThrowEntityNotFoundException_whenBrandNotFound() {
        // Arrange
        UUID nonExistentId = UUID.randomUUID();
        when(brandRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
            () -> brandService.getById(nonExistentId));

        assertEquals("Brand not found", exception.getMessage());
        verify(brandRepository).findById(nonExistentId);
    }

    // ===== SAVE TESTS =====

    @Test
    void save_shouldSaveAndReturnBrand() {
        // Arrange
        Brand newBrand = Brand.builder()
            .name("Sony")
            .description("Japanese electronics company")
            .build();

        Brand savedBrand = Brand.builder()
            .brandId(UUID.randomUUID())
            .name("Sony")
            .description("Japanese electronics company")
            .build();

        when(brandRepository.save(newBrand)).thenReturn(savedBrand);

        // Act
        Brand result = brandService.save(newBrand);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getBrandId());
        assertEquals("Sony", result.getName());
        assertEquals("Japanese electronics company", result.getDescription());

        verify(brandRepository).save(newBrand);
    }

    // ===== UPDATE TESTS =====

    @Test
    void update_shouldUpdateAndReturnBrand_whenBrandExists() {
        // Arrange
        Brand updateData = Brand.builder()
            .name("Samsung Electronics")
            .description("Updated description for Samsung")
            .build();

        Brand updatedBrand = Brand.builder()
            .brandId(testBrandId)
            .name("Samsung Electronics")
            .description("Updated description for Samsung")
            .build();

        when(brandRepository.findById(testBrandId)).thenReturn(Optional.of(testBrand));
        when(brandRepository.save(any(Brand.class))).thenReturn(updatedBrand);

        // Act
        Brand result = brandService.update(testBrandId, updateData);

        // Assert
        assertNotNull(result);
        assertEquals(testBrandId, result.getBrandId());
        assertEquals("Samsung Electronics", result.getName());
        assertEquals("Updated description for Samsung", result.getDescription());

        verify(brandRepository).findById(testBrandId);
        verify(brandRepository).save(any(Brand.class));
    }

    @Test
    void update_shouldThrowEntityNotFoundException_whenBrandNotFound() {
        // Arrange
        UUID nonExistentId = UUID.randomUUID();
        Brand updateData = Brand.builder()
            .name("Some Brand")
            .description("Some description")
            .build();

        when(brandRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
            () -> brandService.update(nonExistentId, updateData));

        assertEquals("Brand not found", exception.getMessage());
        verify(brandRepository).findById(nonExistentId);
        verify(brandRepository, never()).save(any(Brand.class));
    }

    // ===== DELETE BY ID TESTS =====

    @Test
    void deleteById_shouldSoftDeleteBrand() {
        // Arrange
        when(brandRepository.findById(testBrandId)).thenReturn(Optional.of(testBrand));
        when(brandRepository.save(any(Brand.class))).thenReturn(testBrand);

        // Act
        brandService.deleteById(testBrandId);

        // Assert - soft delete should find entity, set deletedAt, and save
        verify(brandRepository).findById(testBrandId);
        verify(brandRepository).save(testBrand);
        assertNotNull(testBrand.getDeletedAt());
    }

    @Test
    void deleteById_shouldThrowEntityNotFoundException_whenBrandNotFound() {
        // Arrange
        UUID nonExistentId = UUID.randomUUID();
        when(brandRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
            () -> brandService.deleteById(nonExistentId));

        assertEquals("Brand not found", exception.getMessage());
        verify(brandRepository).findById(nonExistentId);
        verify(brandRepository, never()).save(any(Brand.class));
    }
}

