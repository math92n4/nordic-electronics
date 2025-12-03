package com.example.nordicelectronics.unit.service;

import com.example.nordicelectronics.entity.*;
import com.example.nordicelectronics.entity.dto.warehouse.WarehouseRequestDTO;
import com.example.nordicelectronics.entity.dto.warehouse.WarehouseResponseDTO;
import com.example.nordicelectronics.repositories.sql.AddressRepository;
import com.example.nordicelectronics.repositories.sql.WarehouseRepository;
import com.example.nordicelectronics.service.WarehouseService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for WarehouseService using Classical testing approach.
 *
 * Testing Strategy:
 * - Mock shared dependencies (repositories) as they represent external systems
 * - Use real value objects (DTOs, domain objects) for internal logic
 * - Follow AAA (Arrange-Act-Assert) pattern
 * - Cover all branches and edge cases for 100% coverage
 *
 * Coverage Goals:
 * - Happy path scenarios for all CRUD operations
 * - Error conditions (EntityNotFoundException)
 * - Edge cases (empty lists, null handling, etc.)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("WarehouseService Unit Tests")
class WarehouseServiceTest {

    @InjectMocks
    private WarehouseService warehouseService;

    @Mock
    private WarehouseRepository warehouseRepository;

    @Mock
    private AddressRepository addressRepository;

    private Warehouse warehouse;
    private Address address;
    private UUID warehouseId;
    private UUID addressId;

    @BeforeEach
    void setUp() {
        // Create test data
        warehouseId = UUID.randomUUID();
        addressId = UUID.randomUUID();

        User user = User.builder()
                .userId(UUID.randomUUID())
                .firstName("Test")
                .lastName("User")
                .email("user@mail.com")
                .phoneNumber("12345678")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .password("password")
                .address(Collections.emptyList())
                .orders(Collections.emptyList())
                .build();

        address = Address.builder()
                .addressId(addressId)
                .user(user)
                .street("Testing Street")
                .streetNumber("42")
                .zip("1234")
                .city("Test City")
                .build();

        Product product = Product.builder()
                .productId(UUID.randomUUID())
                .name("Test Product")
                .description("This is a test product")
                .price(BigDecimal.valueOf(99.99))
                .build();

        Product product2 = Product.builder()
                .productId(UUID.randomUUID())
                .name("Test Product 2")
                .description("This is another test product")
                .price(BigDecimal.valueOf(49.99))
                .build();

        WarehouseProduct wp1 = WarehouseProduct.builder()
                .id(new WarehouseProductKey())
                .product(product)
                .stockQuantity(1)
                .build();

        WarehouseProduct wp2 = WarehouseProduct.builder()
                .id(new WarehouseProductKey())
                .product(product2)
                .stockQuantity(1)
                .build();

        warehouse = Warehouse.builder()
                .warehouseId(warehouseId)
                .name("Test Warehouse")
                .phoneNumber("87654321")
                .address(address)
                .warehouseProducts(Set.of(wp1, wp2))
                .build();
    }

    @Nested
    @DisplayName("getAll() Tests")
    class GetAllTests {

        @Test
        @DisplayName("Should return list of warehouses when warehouses exist")
        void shouldReturnListOfWarehouses_WhenWarehousesExist() {
            // Arrange
            Warehouse warehouse2 = Warehouse.builder()
                    .warehouseId(UUID.randomUUID())
                    .name("Second Warehouse")
                    .phoneNumber("11223344")
                    .address(address)
                    .warehouseProducts(Collections.emptySet())
                    .build();

            when(warehouseRepository.findAll()).thenReturn(List.of(warehouse, warehouse2));

            // Act
            List<WarehouseResponseDTO> result = warehouseService.getAll();

            // Assert
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getName()).isEqualTo("Test Warehouse");
            assertThat(result.get(1).getName()).isEqualTo("Second Warehouse");
            verify(warehouseRepository, times(1)).findAll();
        }

        @Test
        @DisplayName("Should return empty list when no warehouses exist")
        void shouldReturnEmptyList_WhenNoWarehousesExist() {
            // Arrange
            when(warehouseRepository.findAll()).thenReturn(Collections.emptyList());

            // Act
            List<WarehouseResponseDTO> result = warehouseService.getAll();

            // Assert
            assertThat(result).isEmpty();
            verify(warehouseRepository, times(1)).findAll();
        }
    }

    @Nested
    @DisplayName("getById() Tests")
    class GetByIdTests {

        @Test
        @DisplayName("Should return warehouse when warehouse exists")
        void shouldReturnWarehouse_WhenWarehouseExists() {
            // Arrange
            when(warehouseRepository.findById(warehouseId)).thenReturn(Optional.of(warehouse));

            // Act
            WarehouseResponseDTO result = warehouseService.getById(warehouseId);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getWarehouseId()).isEqualTo(warehouseId);
            assertThat(result.getName()).isEqualTo("Test Warehouse");
            assertThat(result.getPhoneNumber()).isEqualTo("87654321");
            verify(warehouseRepository, times(1)).findById(warehouseId);
        }

        @Test
        @DisplayName("Should throw EntityNotFoundException when warehouse does not exist")
        void shouldThrowEntityNotFoundException_WhenWarehouseDoesNotExist() {
            // Arrange
            UUID nonExistentId = UUID.randomUUID();
            when(warehouseRepository.findById(nonExistentId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> warehouseService.getById(nonExistentId))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessage("Warehouse not found");
            verify(warehouseRepository, times(1)).findById(nonExistentId);
        }
    }

    @Nested
    @DisplayName("save() Tests")
    class SaveTests {

        @Test
        @DisplayName("Should save warehouse successfully when valid request provided")
        void shouldSaveWarehouse_WhenValidRequestProvided() {
            // Arrange
            WarehouseRequestDTO requestDTO = WarehouseRequestDTO.builder()
                    .name("New Warehouse")
                    .phoneNumber("99887766")
                    .addressId(addressId)
                    .build();

            when(addressRepository.findById(addressId)).thenReturn(Optional.of(address));
            when(warehouseRepository.save(any(Warehouse.class))).thenReturn(warehouse);

            // Act
            WarehouseResponseDTO result = warehouseService.save(requestDTO);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("Test Warehouse");
            verify(addressRepository, times(1)).findById(addressId);
            verify(warehouseRepository, times(1)).save(any(Warehouse.class));
        }

        @Test
        @DisplayName("Should throw EntityNotFoundException when address does not exist")
        void shouldThrowEntityNotFoundException_WhenAddressDoesNotExist() {
            // Arrange
            UUID nonExistentAddressId = UUID.randomUUID();
            WarehouseRequestDTO requestDTO = WarehouseRequestDTO.builder()
                    .name("New Warehouse")
                    .phoneNumber("99887766")
                    .addressId(nonExistentAddressId)
                    .build();

            when(addressRepository.findById(nonExistentAddressId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> warehouseService.save(requestDTO))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessage("Address not found with id: " + nonExistentAddressId);
            verify(addressRepository, times(1)).findById(nonExistentAddressId);
            verify(warehouseRepository, never()).save(any(Warehouse.class));
        }
    }

    @Nested
    @DisplayName("update() Tests")
    class UpdateTests {

        @Test
        @DisplayName("Should update warehouse when valid request provided")
        void shouldUpdateWarehouse_WhenValidRequestProvided() {
            // Arrange
            UUID newAddressId = UUID.randomUUID();
            Address newAddress = Address.builder()
                    .addressId(newAddressId)
                    .street("New Street")
                    .streetNumber("99")
                    .zip("5678")
                    .city("New City")
                    .build();

            WarehouseRequestDTO requestDTO = WarehouseRequestDTO.builder()
                    .name("Updated Warehouse")
                    .phoneNumber("11112222")
                    .addressId(newAddressId)
                    .build();

            when(warehouseRepository.findById(warehouseId)).thenReturn(Optional.of(warehouse));
            when(addressRepository.findById(newAddressId)).thenReturn(Optional.of(newAddress));
            when(warehouseRepository.save(any(Warehouse.class))).thenReturn(warehouse);

            // Act
            WarehouseResponseDTO result = warehouseService.update(warehouseId, requestDTO);

            // Assert
            assertThat(result).isNotNull();
            verify(warehouseRepository, times(1)).findById(warehouseId);
            verify(addressRepository, times(1)).findById(newAddressId);
            verify(warehouseRepository, times(1)).save(warehouse);
        }

        @Test
        @DisplayName("Should update warehouse without changing address when addressId is null")
        void shouldUpdateWarehouseWithoutChangingAddress_WhenAddressIdIsNull() {
            // Arrange
            WarehouseRequestDTO requestDTO = WarehouseRequestDTO.builder()
                    .name("Updated Warehouse")
                    .phoneNumber("11112222")
                    .addressId(null)  // No address change
                    .build();

            when(warehouseRepository.findById(warehouseId)).thenReturn(Optional.of(warehouse));
            when(warehouseRepository.save(any(Warehouse.class))).thenReturn(warehouse);

            // Act
            WarehouseResponseDTO result = warehouseService.update(warehouseId, requestDTO);

            // Assert
            assertThat(result).isNotNull();
            verify(warehouseRepository, times(1)).findById(warehouseId);
            verify(addressRepository, never()).findById(any(UUID.class));
            verify(warehouseRepository, times(1)).save(warehouse);
        }

        @Test
        @DisplayName("Should throw EntityNotFoundException when warehouse does not exist")
        void shouldThrowEntityNotFoundException_WhenWarehouseDoesNotExist() {
            // Arrange
            UUID nonExistentId = UUID.randomUUID();
            WarehouseRequestDTO requestDTO = WarehouseRequestDTO.builder()
                    .name("Updated Warehouse")
                    .phoneNumber("11112222")
                    .addressId(addressId)
                    .build();

            when(warehouseRepository.findById(nonExistentId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> warehouseService.update(nonExistentId, requestDTO))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessage("Warehouse not found");
            verify(warehouseRepository, times(1)).findById(nonExistentId);
            verify(addressRepository, never()).findById(any(UUID.class));
            verify(warehouseRepository, never()).save(any(Warehouse.class));
        }

        @Test
        @DisplayName("Should throw EntityNotFoundException when new address does not exist")
        void shouldThrowEntityNotFoundException_WhenNewAddressDoesNotExist() {
            // Arrange
            UUID nonExistentAddressId = UUID.randomUUID();
            WarehouseRequestDTO requestDTO = WarehouseRequestDTO.builder()
                    .name("Updated Warehouse")
                    .phoneNumber("11112222")
                    .addressId(nonExistentAddressId)
                    .build();

            when(warehouseRepository.findById(warehouseId)).thenReturn(Optional.of(warehouse));
            when(addressRepository.findById(nonExistentAddressId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> warehouseService.update(warehouseId, requestDTO))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessage("Address not found with id: " + nonExistentAddressId);
            verify(warehouseRepository, times(1)).findById(warehouseId);
            verify(addressRepository, times(1)).findById(nonExistentAddressId);
            verify(warehouseRepository, never()).save(any(Warehouse.class));
        }
    }

    @Nested
    @DisplayName("deleteById() Tests")
    class DeleteByIdTests {

        @Test
        @DisplayName("Should soft delete warehouse when valid id provided")
        void shouldSoftDeleteWarehouse_WhenValidIdProvided() {
            // Arrange
            when(warehouseRepository.findById(warehouseId)).thenReturn(Optional.of(warehouse));
            when(warehouseRepository.save(any(Warehouse.class))).thenReturn(warehouse);

            // Act
            warehouseService.deleteById(warehouseId);

            // Assert - soft delete should find entity, set deletedAt, and save
            verify(warehouseRepository, times(1)).findById(warehouseId);
            verify(warehouseRepository, times(1)).save(warehouse);
            assertThat(warehouse.getDeletedAt()).isNotNull();
        }

        @Test
        @DisplayName("Should throw EntityNotFoundException when warehouse does not exist")
        void shouldThrowEntityNotFoundException_WhenWarehouseDoesNotExist() {
            // Arrange
            UUID nonExistentId = UUID.randomUUID();
            when(warehouseRepository.findById(nonExistentId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> warehouseService.deleteById(nonExistentId))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessage("Warehouse not found");
            verify(warehouseRepository, times(1)).findById(nonExistentId);
            verify(warehouseRepository, never()).save(any(Warehouse.class));
        }
    }
}