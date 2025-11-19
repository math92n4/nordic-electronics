package com.example.nordicelectronics.entity;

import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class OrderProductKey implements Serializable {
    private UUID orderId;
    private UUID productId;
}

