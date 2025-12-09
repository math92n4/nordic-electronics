package com.example.nordicelectronics.entity.mongodb;

import lombok.*;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class WarrantyEmbedded implements Serializable {

    private UUID warrantyId;
    private LocalDate startDate;
    private LocalDate endDate;
    private String description;
}
