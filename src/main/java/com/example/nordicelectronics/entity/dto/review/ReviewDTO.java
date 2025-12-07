package com.example.nordicelectronics.entity.dto.review;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ReviewDTO {

    private UUID productId;
    private UUID userId;
    private UUID orderId;
    private int reviewValue;
    private String title;
    private String comment;
    private Boolean isVerifiedPurchase;

}
