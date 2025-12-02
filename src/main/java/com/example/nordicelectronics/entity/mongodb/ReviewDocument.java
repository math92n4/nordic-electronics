package com.example.nordicelectronics.entity.mongodb;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.UUID;

@Document(collection = "reviews")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
public class ReviewDocument extends BaseDocument {
    
    @Id
    private String id;

    @Field("review_id")
    private UUID reviewId;

    @Field("user_id")
    private UUID userId;

    @Field("order_id")
    private UUID orderId;

    @Field("review_value")
    private int reviewValue;

    @Field("title")
    private String title;

    @Field("comment")
    private String comment;

    @Field("is_verified_purchase")
    private boolean isVerifiedPurchase;

    @Field("product_id")
    private UUID productId;
}

