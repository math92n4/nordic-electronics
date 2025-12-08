package com.example.nordicelectronics.entity.mongodb;

import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class CategoryEmbedded implements Serializable {

    private UUID categoryId;
    private String name;
    private String description;
}
