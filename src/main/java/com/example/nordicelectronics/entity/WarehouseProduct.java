package com.example.nordicelectronics.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import lombok.*;

import java.io.Serializable;

@Entity
@Table(name = "warehouse_product")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class WarehouseProduct implements Serializable {

    @EmbeddedId
    private WarehouseProductKey id;

    @ManyToOne
    @MapsId("warehouseId")
    @JoinColumn(name = "warehouse_id")
    private Warehouse warehouse;

    @ManyToOne
    @MapsId("productId")
    @JoinColumn(name = "product_id")
    @JsonBackReference
    private Product product;

    @Column(nullable = false)
    @Min(0)
    @Max(50)
    private int stockQuantity;
}
