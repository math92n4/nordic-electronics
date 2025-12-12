package com.example.nordicelectronics.service;

import com.example.nordicelectronics.entity.Coupon;
import com.example.nordicelectronics.entity.dto.coupon.CouponRequestDTO;
import com.example.nordicelectronics.entity.dto.coupon.CouponResponseDTO;
import com.example.nordicelectronics.entity.enums.DiscountType;
import com.example.nordicelectronics.entity.mapper.CouponMapper;
import com.example.nordicelectronics.entity.validators.CouponValidator;
import com.example.nordicelectronics.repositories.sql.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository couponRepository;

    public CouponResponseDTO getCouponById(UUID couponId) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new RuntimeException("Coupon not found with id: " + couponId));
        return CouponMapper.toResponseDTO(coupon);
    }

    public List<CouponResponseDTO> getAllCouponsByOrderId(UUID orderId) {
        // TODO: implement method to get coupons by order ID
        return couponRepository.findAllByIsActive(true).stream()
                .map(CouponMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    public List<CouponResponseDTO> getAllActiveCoupons() {
        return couponRepository.findAllByIsActive(true).stream()
                .map(CouponMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    public List<CouponResponseDTO> getAllInactiveCoupons() {
        return couponRepository.findAllByIsActive(false).stream()
                .map(CouponMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    public CouponResponseDTO save(CouponRequestDTO dto) {
        Coupon coupon = CouponMapper.toEntity(dto);
        if (!isValid(coupon)) {
            throw new IllegalArgumentException();
        }
        Coupon saved = couponRepository.save(coupon);
        return CouponMapper.toResponseDTO(saved);
    }

    private static boolean isValid(Coupon coupon) {
        if (!CouponValidator.hasValidCouponCodeLength(coupon.getCode())) return false;
        if (!CouponValidator.isValidDiscountType(coupon.getDiscountType())) return false;
        if (!CouponValidator.minimumOrderValueIsPositive(coupon.getMinimumOrderValue())) return false;
        if (!CouponValidator.discountValueIsPositive(coupon.getDiscountValue())) return false;
        if (!CouponValidator.isExpiryDateValid(coupon.getExpiryDate())) return false;
        if (!CouponValidator.canBeUsed(coupon.getUsageLimit(), coupon.getTimesUsed())) return false;
        if (!CouponValidator.isValidAtTimeAndDate(
                coupon.getCreatedAt(),
                coupon.getExpiryDate().atStartOfDay(),
                LocalDateTime.now())) return false;

        if (coupon.getDiscountType() == DiscountType.percentage) {
            if (!CouponValidator.isValidPercentage(coupon.getDiscountValue())) return false;
        }

        return true;
    }

}
