package com.example.nordicelectronics.service.neo4j;

import com.example.nordicelectronics.entity.neo4j.CouponNode;
import com.example.nordicelectronics.repositories.neo4j.CouponNeo4jRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CouponNeo4jService {

    private final CouponNeo4jRepository couponNeo4jRepository;

    public List<CouponNode> getAll() {
        return couponNeo4jRepository.findAll();
    }

    public CouponNode getByCouponId(UUID couponId) {
        return couponNeo4jRepository.findByCouponId(couponId)
                .orElseThrow(() -> new RuntimeException("Coupon not found with ID: " + couponId));
    }

    public CouponNode getByCode(String code) {
        return couponNeo4jRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Coupon not found with code: " + code));
    }

    public List<CouponNode> getActiveCoupons() {
        return couponNeo4jRepository.findByIsActive(true);
    }

    public CouponNode save(CouponNode couponNode) {
        if (couponNode.getCouponId() == null) {
            couponNode.setCouponId(UUID.randomUUID());
        }
        return couponNeo4jRepository.save(couponNode);
    }

    public CouponNode update(UUID couponId, CouponNode couponNode) {
        CouponNode existing = getByCouponId(couponId);

        existing.setCode(couponNode.getCode());
        existing.setDiscountType(couponNode.getDiscountType());
        existing.setDiscountValue(couponNode.getDiscountValue());
        existing.setMinimumOrderValue(couponNode.getMinimumOrderValue());
        existing.setExpiryDate(couponNode.getExpiryDate());
        existing.setUsageLimit(couponNode.getUsageLimit());
        existing.setTimesUsed(couponNode.getTimesUsed());
        existing.setActive(couponNode.isActive());

        return couponNeo4jRepository.save(existing);
    }

    public void deleteByCouponId(UUID couponId) {
        couponNeo4jRepository.deleteByCouponId(couponId);
    }
}

