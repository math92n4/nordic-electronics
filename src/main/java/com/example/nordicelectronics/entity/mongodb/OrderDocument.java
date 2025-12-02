package com.example.nordicelectronics.entity.mongodb;

import com.example.nordicelectronics.entity.enums.OrderStatus;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Document(collection = "orders")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class OrderDocument extends BaseDocument {

    @Id
    private String id;

    @Field("order_id")
    private UUID orderId;

    @Field("user_id")
    private UUID userId;

    @Field("payment_id")
    private UUID paymentId;

    @Field("address_id")
    private UUID addressId;

    @Field("order_date")
    private LocalDateTime orderDate;

    @Field("status")
    private OrderStatus orderStatus;

    @Field("total_amount")
    private BigDecimal totalAmount;

    @Field("subtotal")
    private BigDecimal subtotal;

    @Field("tax_amount")
    private BigDecimal taxAmount;

    @Field("shipping_cost")
    private BigDecimal shippingCost;

    @Field("discount_amount")
    private BigDecimal discountAmount;

    @Field("order_products")
    @Builder.Default
    private List<OrderProductEmbedded> orderProducts = new ArrayList<>();

    @Field("coupon_id")
    private UUID couponId;
}

