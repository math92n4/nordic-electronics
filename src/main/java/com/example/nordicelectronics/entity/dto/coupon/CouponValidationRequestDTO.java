package com.example.nordicelectronics.entity.dto.coupon;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CouponValidationRequestDTO {
    private String couponCode;
    private BigDecimal orderSubtotal;
}
