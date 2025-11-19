package com.example.nordicelectronics.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "order_coupon")
@IdClass(OrderCouponKey.class)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class OrderCoupon {

    @Id
    @Column(name = "order_id")
    private UUID orderId;

    @Id
    @Column(name = "coupon_id")
    private UUID couponId;

    @ManyToOne
    @JoinColumn(name = "order_id", insertable = false, updatable = false)
    private Order order;

    @ManyToOne
    @JoinColumn(name = "coupon_id", insertable = false, updatable = false)
    private Coupon coupon;
}

