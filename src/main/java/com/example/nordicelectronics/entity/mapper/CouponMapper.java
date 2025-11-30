package com.example.nordicelectronics.entity.mapper;

import com.example.nordicelectronics.entity.Coupon;
import com.example.nordicelectronics.entity.dto.coupon.CouponRequestDTO;
import com.example.nordicelectronics.entity.dto.coupon.CouponResponseDTO;

public class CouponMapper {

    public static CouponResponseDTO toResponseDTO(Coupon coupon) {
        if (coupon == null) {
            return null;
        }

        return CouponResponseDTO.builder()
                .couponId(coupon.getCouponId())
                .code(coupon.getCode())
                .discountType(coupon.getDiscountType())
                .discountValue(coupon.getDiscountValue())
                .minimumOrderValue(coupon.getMinimumOrderValue())
                .expiryDate(coupon.getExpiryDate())
                .usageLimit(coupon.getUsageLimit())
                .timesUsed(coupon.getTimesUsed())
                .isActive(coupon.isActive())
                .build();
    }

    public static Coupon toEntity(CouponRequestDTO dto) {
        if (dto == null) {
            return null;
        }

        return Coupon.builder()
                .code(dto.getCode())
                .discountType(dto.getDiscountType())
                .discountValue(dto.getDiscountValue())
                .minimumOrderValue(dto.getMinimumOrderValue())
                .expiryDate(dto.getExpiryDate())
                .usageLimit(dto.getUsageLimit() != null ? dto.getUsageLimit() : 0)
                .timesUsed(0) // New coupons start with 0 uses
                .isActive(dto.getIsActive() != null ? dto.getIsActive() : true)
                .build();
    }
}

