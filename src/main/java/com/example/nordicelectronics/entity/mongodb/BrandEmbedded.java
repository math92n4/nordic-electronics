package com.example.nordicelectronics.entity.mongodb;

import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class BrandEmbedded implements Serializable {

    private UUID brandId;
    private String name;
    private String description;
}
