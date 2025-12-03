package com.example.nordicelectronics.service.mongodb;

import com.example.nordicelectronics.entity.mongodb.CouponDocument;
import com.example.nordicelectronics.repositories.mongodb.CouponMongoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@ConditionalOnProperty(name = "spring.data.mongodb.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class CouponMongoService {

    private final CouponMongoRepository couponMongoRepository;

    public List<CouponDocument> getAll() {
        return couponMongoRepository.findAll();
    }

    public CouponDocument getByCouponId(UUID couponId) {
        return couponMongoRepository.findByCouponId(couponId)
                .orElseThrow(() -> new RuntimeException("Coupon not found with ID: " + couponId));
    }

    public CouponDocument getByCode(String code) {
        return couponMongoRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Coupon not found with code: " + code));
    }

    public List<CouponDocument> getActiveCoupons() {
        return couponMongoRepository.findByIsActive(true);
    }

    public CouponDocument save(CouponDocument couponDocument) {
        if (couponDocument.getCouponId() == null) {
            couponDocument.setCouponId(UUID.randomUUID());
        }
        return couponMongoRepository.save(couponDocument);
    }

    public CouponDocument update(UUID couponId, CouponDocument couponDocument) {
        CouponDocument existing = getByCouponId(couponId);
        
        existing.setCode(couponDocument.getCode());
        existing.setDiscountType(couponDocument.getDiscountType());
        existing.setDiscountValue(couponDocument.getDiscountValue());
        existing.setMinimumOrderValue(couponDocument.getMinimumOrderValue());
        existing.setExpiryDate(couponDocument.getExpiryDate());
        existing.setUsageLimit(couponDocument.getUsageLimit());
        existing.setTimesUsed(couponDocument.getTimesUsed());
        existing.setActive(couponDocument.isActive());

        return couponMongoRepository.save(existing);
    }

    public void deleteByCouponId(UUID couponId) {
        couponMongoRepository.deleteByCouponId(couponId);
    }
}

