package com.example.nordicelectronics.entity.mongodb;

import com.example.nordicelectronics.entity.enums.DiscountType;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class CouponEmbedded implements Serializable {

    private UUID couponId;
    private String code;
    private DiscountType discountType;
    private BigDecimal discountValue;
}
