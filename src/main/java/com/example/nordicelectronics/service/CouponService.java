package com.example.nordicelectronics.service;

import com.example.nordicelectronics.entity.Coupon;
import com.example.nordicelectronics.entity.dto.coupon.CouponRequestDTO;
import com.example.nordicelectronics.entity.dto.coupon.CouponResponseDTO;
import com.example.nordicelectronics.entity.mapper.CouponMapper;
import com.example.nordicelectronics.repositories.sql.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
        Coupon saved = couponRepository.save(coupon);
        return CouponMapper.toResponseDTO(saved);
    }

}
