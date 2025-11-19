package com.example.nordicelectronics.service.mongo;

import com.example.nordicelectronics.document.CouponDocument;
import com.example.nordicelectronics.repositories.mongodb.CouponMongoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CouponMongoService {

    private final CouponMongoRepository couponMongoRepository;

    public List<CouponDocument> getAll() {
        return couponMongoRepository.findAll();
    }

    public CouponDocument getById(String id) {
        return couponMongoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Coupon not found with id: " + id));
    }

    public CouponDocument save(CouponDocument coupon) {
        return couponMongoRepository.save(coupon);
    }

    public CouponDocument update(String id, CouponDocument coupon) {
        CouponDocument existing = getById(id);
        existing.setCode(coupon.getCode());
        existing.setDiscountType(coupon.getDiscountType());
        existing.setDiscountValue(coupon.getDiscountValue());
        existing.setMinimumOrderValue(coupon.getMinimumOrderValue());
        existing.setExpiryDate(coupon.getExpiryDate());
        existing.setUsageLimit(coupon.getUsageLimit());
        existing.setTimesUsed(coupon.getTimesUsed());
        existing.setActive(coupon.isActive());
        return couponMongoRepository.save(existing);
    }

    public void deleteById(String id) {
        couponMongoRepository.deleteById(id);
    }

    public CouponDocument getByCode(String code) {
        return couponMongoRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Coupon not found with code: " + code));
    }
}

