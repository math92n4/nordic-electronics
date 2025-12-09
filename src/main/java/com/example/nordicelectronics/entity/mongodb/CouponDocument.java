package com.example.nordicelectronics.entity.mongodb;

import com.example.nordicelectronics.entity.enums.DiscountType;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Document(collection = "coupons")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class CouponDocument extends BaseDocument {
    
    @Id
    private String id;

    @Field("coupon_id")
    @Indexed(unique = true)
    private UUID couponId;

    @Field("code")
    @Indexed(unique = true)
    private String code;

    @Field("discount_type")
    private DiscountType discountType;

    @Field("discount_value")
    private BigDecimal discountValue;

    @Field("minimum_order_value")
    private BigDecimal minimumOrderValue;

    @Field("expiry_date")
    private LocalDate expiryDate;

    @Field("usage_limit")
    private int usageLimit;

    @Field("times_used")
    private int timesUsed;

    @Field("is_active")
    private boolean isActive;
}
