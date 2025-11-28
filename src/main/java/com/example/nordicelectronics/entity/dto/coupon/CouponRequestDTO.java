package com.example.nordicelectronics.entity.dto.coupon;

import com.example.nordicelectronics.entity.enums.DiscountType;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CouponRequestDTO {
    private String code;
    private DiscountType discountType;
    private BigDecimal discountValue;
    private BigDecimal minimumOrderValue;
    private LocalDate expiryDate;
    private Integer usageLimit;
    private Boolean isActive;
}

