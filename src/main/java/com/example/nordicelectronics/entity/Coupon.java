package com.example.nordicelectronics.entity;

import com.example.nordicelectronics.entity.enums.DiscountType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Table(name = "coupon")
public class Coupon {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "coupon_id", updatable = false, nullable = false)
    private UUID couponId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @Column(name = "code", nullable = false)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", nullable = false)
    private DiscountType discountType;

    @Column(name = "discount_value", nullable = false, precision = 19, scale = 2)
    private BigDecimal discountValue;

    @Column(name = "minimum_order_value", nullable = false, precision = 19, scale = 2)
    private BigDecimal minimumOrderValue;

    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;

    @Column(name = "usage_limit", nullable = false)
    private int usageLimit;

    @Column(name = "times_used", nullable = false)
    private int timesUsed;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;


}
