package com.example.nordicelectronics.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLRestriction;

import java.util.UUID;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@SQLRestriction("deleted_at IS NULL")
public class Address extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "address_id", updatable = false, nullable = false)
    private UUID addressId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonBackReference
    private User user;

    @Column(nullable = false)
    private String street;

    @Column(nullable = false)
    private String streetNumber;

    @Column(nullable = false)
    private String zip;

    @Column(nullable = false)
    private String city;

}