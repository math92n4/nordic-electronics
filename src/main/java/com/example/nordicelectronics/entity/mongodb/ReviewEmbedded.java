package com.example.nordicelectronics.entity.mongodb;

import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ReviewEmbedded implements Serializable {

    private UUID reviewId;
    private UUID userId;
    private String userName;
    private int reviewValue;
    private String title;
    private String comment;
    private boolean isVerifiedPurchase;
    private LocalDateTime createdAt;
}
