package com.example.nordicelectronics.entity;

import com.example.nordicelectronics.entity.enums.DiscountType;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcType;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "coupon")
@SQLRestriction("deleted_at IS NULL")
public class Coupon extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "coupon_id", updatable = false, nullable = false)
    private UUID couponId;

    @Column(name = "code", nullable = false, unique = true)
    @Size(min = 3, max = 20) // BVA: 3-20 characters
    private String code;

    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(name = "discount_type", nullable = false)
    private DiscountType discountType;

    @Column(name = "discount_value", nullable = false, precision = 12, scale = 2)
    private BigDecimal discountValue;

    @Column(name = "minimum_order_value", nullable = false, precision = 12, scale = 2)
    private BigDecimal minimumOrderValue;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "usage_limit", nullable = false)
    private int usageLimit;

    @Column(name = "times_used")
    private int timesUsed;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;
}
