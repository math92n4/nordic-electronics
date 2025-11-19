package com.example.nordicelectronics.entity;

import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class OrderCouponKey implements Serializable {
    private UUID orderId;
    private UUID couponId;
}

