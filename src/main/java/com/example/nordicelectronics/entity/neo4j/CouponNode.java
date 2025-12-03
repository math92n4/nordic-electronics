package com.example.nordicelectronics.entity.neo4j;

import com.example.nordicelectronics.entity.enums.DiscountType;
import lombok.*;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.support.UUIDStringGenerator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Node("Coupon")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class CouponNode extends BaseNode {

    @Id
    @GeneratedValue(generatorClass = UUIDStringGenerator.class)
    private String id;

    @Property("couponId")
    private UUID couponId;

    @Property("code")
    private String code;

    @Property("discountType")
    private DiscountType discountType;

    @Property("discountValue")
    private BigDecimal discountValue;

    @Property("minimumOrderValue")
    private BigDecimal minimumOrderValue;

    @Property("expiryDate")
    private LocalDate expiryDate;

    @Property("usageLimit")
    private int usageLimit;

    @Property("timesUsed")
    private int timesUsed;

    @Property("isActive")
    private boolean isActive;
}

