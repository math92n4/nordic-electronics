package com.example.nordicelectronics.unit.service;

import com.example.nordicelectronics.entity.Warranty;
import com.example.nordicelectronics.repositories.sql.WarrantyRepository;
import com.example.nordicelectronics.service.WarrantyService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WarrantyServiceTest {

    @Mock
    private WarrantyRepository warrantyRepository;

    @InjectMocks
    private WarrantyService warrantyService;

    private Warranty testWarranty;
    private UUID testWarrantyId;

    @BeforeEach
    void setUp() {
        testWarrantyId = UUID.randomUUID();
        testWarranty = Warranty.builder()
            .warrantyId(testWarrantyId)
            .startDate(LocalDate.of(2025, 12, 2))
            .endDate(LocalDate.of(2027, 12, 2))
            .description("2-year standard warranty")
            .build();
    }

    // ===== GET ALL TESTS =====

    @Test
    void getAll_shouldReturnListOfWarranties() {
        // Arrange
        Warranty warranty2 = Warranty.builder()
            .warrantyId(UUID.randomUUID())
            .startDate(LocalDate.of(2025, 12, 2))
            .endDate(LocalDate.of(2028, 12, 2))
            .description("3-year extended warranty")
            .build();
        List<Warranty> warranties = Arrays.asList(testWarranty, warranty2);
        when(warrantyRepository.findAll()).thenReturn(warranties);

        // Act
        List<Warranty> result = warrantyService.getAll();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("2-year standard warranty", result.get(0).getDescription());
        assertEquals("3-year extended warranty", result.get(1).getDescription());

        verify(warrantyRepository).findAll();
    }

    @Test
    void getAll_shouldReturnEmptyList_whenNoWarrantiesExist() {
        // Arrange
        when(warrantyRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<Warranty> result = warrantyService.getAll();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(warrantyRepository).findAll();
    }

    // ===== GET BY ID TESTS =====

    @Test
    void getById_shouldReturnWarranty_whenWarrantyExists() {
        // Arrange
        when(warrantyRepository.findById(testWarrantyId)).thenReturn(Optional.of(testWarranty));

        // Act
        Warranty result = warrantyService.getById(testWarrantyId);

        // Assert
        assertNotNull(result);
        assertEquals(testWarrantyId, result.getWarrantyId());
        assertEquals(LocalDate.of(2025, 12, 2), result.getStartDate());
        assertEquals(LocalDate.of(2027, 12, 2), result.getEndDate());
        assertEquals("2-year standard warranty", result.getDescription());

        verify(warrantyRepository).findById(testWarrantyId);
    }

    @Test
    void getById_shouldThrowEntityNotFoundException_whenWarrantyNotFound() {
        // Arrange
        UUID nonExistentId = UUID.randomUUID();
        when(warrantyRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
            () -> warrantyService.getById(nonExistentId));

        assertEquals("Brand not found", exception.getMessage());
        verify(warrantyRepository).findById(nonExistentId);
    }

    // ===== SAVE TESTS =====

    @Test
    void save_shouldSaveAndReturnWarranty() {
        // Arrange
        Warranty newWarranty = Warranty.builder()
            .startDate(LocalDate.of(2025, 12, 2))
            .endDate(LocalDate.of(2026, 12, 2))
            .description("1-year basic warranty")
            .build();

        Warranty savedWarranty = Warranty.builder()
            .warrantyId(UUID.randomUUID())
            .startDate(LocalDate.of(2025, 12, 2))
            .endDate(LocalDate.of(2026, 12, 2))
            .description("1-year basic warranty")
            .build();

        when(warrantyRepository.save(newWarranty)).thenReturn(savedWarranty);

        // Act
        Warranty result = warrantyService.save(newWarranty);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getWarrantyId());
        assertEquals(LocalDate.of(2025, 12, 2), result.getStartDate());
        assertEquals(LocalDate.of(2026, 12, 2), result.getEndDate());
        assertEquals("1-year basic warranty", result.getDescription());

        verify(warrantyRepository).save(newWarranty);
    }

    // ===== UPDATE TESTS =====

    @Test
    void update_shouldUpdateAndReturnWarranty_whenWarrantyExists() {
        // Arrange
        Warranty updateData = Warranty.builder()
            .startDate(LocalDate.of(2025, 12, 2))
            .endDate(LocalDate.of(2030, 12, 2))
            .description("5-year premium warranty")
            .build();

        Warranty updatedWarranty = Warranty.builder()
            .warrantyId(testWarrantyId)
            .startDate(LocalDate.of(2025, 12, 2))
            .endDate(LocalDate.of(2030, 12, 2))
            .description("5-year premium warranty")
            .build();

        when(warrantyRepository.findById(testWarrantyId)).thenReturn(Optional.of(testWarranty));
        when(warrantyRepository.save(any(Warranty.class))).thenReturn(updatedWarranty);

        // Act
        Warranty result = warrantyService.update(testWarrantyId, updateData);

        // Assert
        assertNotNull(result);
        assertEquals(testWarrantyId, result.getWarrantyId());
        assertEquals(LocalDate.of(2025, 12, 2), result.getStartDate());
        assertEquals(LocalDate.of(2030, 12, 2), result.getEndDate());
        assertEquals("5-year premium warranty", result.getDescription());

        verify(warrantyRepository).findById(testWarrantyId);
        verify(warrantyRepository).save(any(Warranty.class));
    }

    @Test
    void update_shouldThrowEntityNotFoundException_whenWarrantyNotFound() {
        // Arrange
        UUID nonExistentId = UUID.randomUUID();
        Warranty updateData = Warranty.builder()
            .startDate(LocalDate.of(2025, 12, 2))
            .endDate(LocalDate.of(2026, 12, 2))
            .description("Some warranty")
            .build();

        when(warrantyRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
            () -> warrantyService.update(nonExistentId, updateData));

        assertEquals("Brand not found", exception.getMessage());
        verify(warrantyRepository).findById(nonExistentId);
        verify(warrantyRepository, never()).save(any(Warranty.class));
    }

    // ===== DELETE BY ID TESTS =====

    @Test
    void deleteById_shouldCallRepositoryDeleteById() {
        // Arrange
        doNothing().when(warrantyRepository).deleteById(testWarrantyId);

        // Act
        warrantyService.deleteById(testWarrantyId);

        // Assert
        verify(warrantyRepository).deleteById(testWarrantyId);
    }
}

