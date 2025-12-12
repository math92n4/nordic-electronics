package com.example.nordicelectronics.unit.service;

import com.example.nordicelectronics.entity.Coupon;
import com.example.nordicelectronics.entity.dto.coupon.CouponRequestDTO;
import com.example.nordicelectronics.entity.dto.coupon.CouponResponseDTO;
import com.example.nordicelectronics.entity.enums.DiscountType;
import com.example.nordicelectronics.repositories.sql.CouponRepository;
import com.example.nordicelectronics.service.CouponService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.time.LocalDateTime.now;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CouponServiceTest {

    @Mock
    private CouponRepository couponRepository;

    @InjectMocks
    private CouponService couponService;

    private Coupon testCoupon;
    private UUID testCouponId;

    @BeforeEach
    void setUp() {
        testCouponId = UUID.randomUUID();
        testCoupon = Coupon.builder()
            .couponId(testCouponId)
            .code("WINTER25")
            .discountType(DiscountType.percentage)
            .discountValue(BigDecimal.valueOf(25))
            .minimumOrderValue(BigDecimal.valueOf(100))
            .expiryDate(LocalDate.of(2025, 12, 31))
            .usageLimit(50)
            .timesUsed(10)
            .isActive(true)
            .build();
    }

    // ===== GET COUPON BY ID TESTS =====

    @Test
    void getCouponById_shouldReturnCouponResponseDTO_whenCouponExists() {
        // Arrange
        when(couponRepository.findById(testCouponId)).thenReturn(Optional.of(testCoupon));

        // Act
        CouponResponseDTO result = couponService.getCouponById(testCouponId);

        // Assert
        assertNotNull(result);
        assertEquals(testCouponId, result.getCouponId());
        assertEquals("WINTER25", result.getCode());
        assertEquals(DiscountType.percentage, result.getDiscountType());
        assertEquals(BigDecimal.valueOf(25), result.getDiscountValue());
        assertEquals(BigDecimal.valueOf(100), result.getMinimumOrderValue());
        assertEquals(LocalDate.of(2025, 12, 31), result.getExpiryDate());
        assertEquals(50, result.getUsageLimit());
        assertEquals(10, result.getTimesUsed());
        assertTrue(result.getIsActive());

        verify(couponRepository).findById(testCouponId);
    }

    @Test
    void getCouponById_shouldThrowRuntimeException_whenCouponNotFound() {
        // Arrange
        UUID nonExistentId = UUID.randomUUID();
        when(couponRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> couponService.getCouponById(nonExistentId));
        
        assertTrue(exception.getMessage().contains("Coupon not found with id: " + nonExistentId));
        verify(couponRepository).findById(nonExistentId);
    }

    // ===== GET ALL COUPONS BY ORDER ID TESTS =====

    @Test
    void getAllCouponsByOrderId_shouldReturnActiveCoupons() {
        // Note: Current implementation ignores orderId and returns all active coupons
        // This test reflects current behavior but indicates incomplete implementation
        
        // Arrange
        UUID orderId = UUID.randomUUID();
        List<Coupon> activeCoupons = List.of(testCoupon);
        when(couponRepository.findAllByIsActive(true)).thenReturn(activeCoupons);

        // Act
        List<CouponResponseDTO> result = couponService.getAllCouponsByOrderId(orderId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("WINTER25", result.get(0).getCode());
        assertTrue(result.get(0).getIsActive());

        verify(couponRepository).findAllByIsActive(true);
        // Note: orderId is not actually used in current implementation
    }

    @Test
    void getAllCouponsByOrderId_shouldReturnEmptyList_whenNoActiveCoupons() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        when(couponRepository.findAllByIsActive(true)).thenReturn(Collections.emptyList());

        // Act
        List<CouponResponseDTO> result = couponService.getAllCouponsByOrderId(orderId);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(couponRepository).findAllByIsActive(true);
    }

    // ===== GET ALL ACTIVE COUPONS TESTS =====

    @Test
    void getAllActiveCoupons_shouldReturnActiveCoupons() {
        // Arrange
        Coupon activeCoupon2 = Coupon.builder()
            .couponId(UUID.randomUUID())
            .code("SUMMER30")
            .discountType(DiscountType.fixed_amount)
            .discountValue(BigDecimal.valueOf(50))
            .minimumOrderValue(BigDecimal.valueOf(200))
            .expiryDate(LocalDate.of(2026, 6, 30))
            .usageLimit(50)
            .timesUsed(5)
            .isActive(true)
            .build();

        List<Coupon> activeCoupons = Arrays.asList(testCoupon, activeCoupon2);
        when(couponRepository.findAllByIsActive(true)).thenReturn(activeCoupons);

        // Act
        List<CouponResponseDTO> result = couponService.getAllActiveCoupons();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        
        // Verify first coupon
        CouponResponseDTO first = result.get(0);
        assertEquals("WINTER25", first.getCode());
        assertEquals(DiscountType.percentage, first.getDiscountType());
        assertTrue(first.getIsActive());
        
        // Verify second coupon
        CouponResponseDTO second = result.get(1);
        assertEquals("SUMMER30", second.getCode());
        assertEquals(DiscountType.fixed_amount, second.getDiscountType());
        assertTrue(second.getIsActive());

        verify(couponRepository).findAllByIsActive(true);
    }

    @Test
    void getAllActiveCoupons_shouldReturnEmptyList_whenNoActiveCoupons() {
        // Arrange
        when(couponRepository.findAllByIsActive(true)).thenReturn(Collections.emptyList());

        // Act
        List<CouponResponseDTO> result = couponService.getAllActiveCoupons();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(couponRepository).findAllByIsActive(true);
    }

    // ===== GET ALL INACTIVE COUPONS TESTS =====

    @Test
    void getAllInactiveCoupons_shouldReturnInactiveCoupons() {
        // Arrange
        Coupon inactiveCoupon = Coupon.builder()
            .couponId(UUID.randomUUID())
            .code("EXPIRED20")
            .discountType(DiscountType.percentage)
            .discountValue(BigDecimal.valueOf(20))
            .minimumOrderValue(BigDecimal.valueOf(50))
            .expiryDate(LocalDate.of(2024, 12, 31))
            .usageLimit(50)
            .timesUsed(50)
            .isActive(false)
            .build();

        List<Coupon> inactiveCoupons = List.of(inactiveCoupon);
        when(couponRepository.findAllByIsActive(false)).thenReturn(inactiveCoupons);

        // Act
        List<CouponResponseDTO> result = couponService.getAllInactiveCoupons();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        
        CouponResponseDTO resultCoupon = result.get(0);
        assertEquals("EXPIRED20", resultCoupon.getCode());
        assertEquals(DiscountType.percentage, resultCoupon.getDiscountType());
        assertFalse(resultCoupon.getIsActive());
        assertEquals(50, resultCoupon.getTimesUsed());
        assertEquals(50, resultCoupon.getUsageLimit());

        verify(couponRepository).findAllByIsActive(false);
    }

    @Test
    void getAllInactiveCoupons_shouldReturnEmptyList_whenNoInactiveCoupons() {
        // Arrange
        when(couponRepository.findAllByIsActive(false)).thenReturn(Collections.emptyList());

        // Act
        List<CouponResponseDTO> result = couponService.getAllInactiveCoupons();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(couponRepository).findAllByIsActive(false);
    }

    // ===== SAVE COUPON TESTS =====

    @Test
    void save_shouldCreateAndReturnCoupon_whenValidRequestDTO() {
        // Arrange
        CouponRequestDTO requestDTO = CouponRequestDTO.builder()
            .code("NEWCODE15")
            .discountType(DiscountType.percentage)
            .discountValue(BigDecimal.valueOf(15))
            .minimumOrderValue(BigDecimal.valueOf(75))
            .expiryDate(LocalDate.of(2026, 3, 15))
            .usageLimit(50)
            .isActive(true)
            .build();

        Coupon savedCoupon = Coupon.builder()
            .couponId(UUID.randomUUID())
            .code("NEWCODE15")
            .discountType(DiscountType.percentage)
            .discountValue(BigDecimal.valueOf(15))
            .minimumOrderValue(BigDecimal.valueOf(75))
            .expiryDate(LocalDate.of(2026, 3, 15))
            .usageLimit(50)
            .timesUsed(0)
            .isActive(true)
            .build();

        when(couponRepository.save(any(Coupon.class))).thenReturn(savedCoupon);

        // Act
        CouponResponseDTO result = couponService.save(requestDTO);

        // Assert
        assertNotNull(result);
        assertEquals("NEWCODE15", result.getCode());
        assertEquals(DiscountType.percentage, result.getDiscountType());
        assertEquals(BigDecimal.valueOf(15), result.getDiscountValue());
        assertEquals(BigDecimal.valueOf(75), result.getMinimumOrderValue());
        assertEquals(LocalDate.of(2026, 3, 15), result.getExpiryDate());
        assertEquals(50, result.getUsageLimit());
        assertEquals(0, result.getTimesUsed());
        assertTrue(result.getIsActive());

        verify(couponRepository).save(any(Coupon.class));
    }

    @Test
    void save_shouldHandleFixedAmountDiscountType() {
        // Arrange
        CouponRequestDTO requestDTO = CouponRequestDTO.builder()
            .code("FIXED100")
            .discountType(DiscountType.fixed_amount)
            .discountValue(BigDecimal.valueOf(100))
            .minimumOrderValue(BigDecimal.valueOf(500))
            .expiryDate(LocalDate.of(2025, 12, 25))
            .usageLimit(25)
            .isActive(true)
            .build();

        Coupon savedCoupon = Coupon.builder()
            .couponId(UUID.randomUUID())
            .code("FIXED100")
            .discountType(DiscountType.fixed_amount)
            .discountValue(BigDecimal.valueOf(100))
            .minimumOrderValue(BigDecimal.valueOf(500))
            .expiryDate(LocalDate.of(2025, 12, 25))
            .usageLimit(25)
            .timesUsed(0)
            .isActive(true)
            .createdAt(now().minusDays(1))
            .updatedAt(now())
            .build();

        when(couponRepository.save(any(Coupon.class))).thenReturn(savedCoupon);

        // Act
        CouponResponseDTO result = couponService.save(requestDTO);

        // Assert
        assertNotNull(result);
        assertEquals("FIXED100", result.getCode());
        assertEquals(DiscountType.fixed_amount, result.getDiscountType());
        assertEquals(BigDecimal.valueOf(100), result.getDiscountValue());

        verify(couponRepository).save(any(Coupon.class));
    }

    // ===== EDGE CASE AND ERROR HANDLING TESTS =====

    @Test
    void save_shouldHandleRepositoryException() {
        // Arrange
        CouponRequestDTO requestDTO = CouponRequestDTO.builder()
            .code("TESTCODE")
            .discountType(DiscountType.percentage)
            .discountValue(BigDecimal.valueOf(10))
            .minimumOrderValue(BigDecimal.valueOf(50))
            .expiryDate(LocalDate.of(2026, 1, 1))
            .usageLimit(10)
            .isActive(true)
            .build();

        when(couponRepository.save(any(Coupon.class)))
            .thenThrow(new RuntimeException("Database connection failed"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> couponService.save(requestDTO));

        verify(couponRepository).save(any(Coupon.class));
    }

    @Test
    void getAllActiveCoupons_shouldHandleRepositoryException() {
        // Arrange
        when(couponRepository.findAllByIsActive(true))
            .thenThrow(new RuntimeException("Database query failed"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> couponService.getAllActiveCoupons());

        verify(couponRepository).findAllByIsActive(true);
    }
}
