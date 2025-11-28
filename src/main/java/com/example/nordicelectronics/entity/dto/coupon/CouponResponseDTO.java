package com.example.nordicelectronics.entity.dto.coupon;

import com.example.nordicelectronics.entity.enums.DiscountType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
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
public class CouponResponseDTO {
    private UUID couponId;
    private String code;
    private DiscountType discountType;
    private BigDecimal discountValue;
    private BigDecimal minimumOrderValue;
    private LocalDate expiryDate;
    private Integer usageLimit;
    private Integer timesUsed;
    private Boolean isActive;
}

