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
        try {
            validateCoupon(coupon);
            Coupon saved = couponRepository.save(coupon);
            return CouponMapper.toResponseDTO(saved);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Coupon is not saved, not valid");
        }
    }

    private static void validateCoupon(Coupon coupon) {
        if (coupon == null) {
            throw new IllegalArgumentException("Coupon cannot be null");
        }

        CouponValidator.validateCouponCode(coupon.getCode());
        CouponValidator.validateMinimumOrderValue(coupon.getMinimumOrderValue());
        CouponValidator.validateDiscountValue(coupon.getDiscountValue());

        CouponValidator.validateNextUse(coupon.getUsageLimit(), coupon.getTimesUsed());

        /*CouponValidator.validateDateTimeInRange(
                coupon.getCreatedAt(),
                coupon.getExpiryDate().atStartOfDay(),
                LocalDateTime.now()
        );*/

        if (coupon.getDiscountType() == DiscountType.percentage) {
            CouponValidator.validatePercentage(coupon.getDiscountValue());
        }
    }

}
