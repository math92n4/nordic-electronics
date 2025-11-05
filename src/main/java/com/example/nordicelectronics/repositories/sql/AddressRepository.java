package com.example.nordicelectronics.repositories.sql;

import com.example.nordicelectronics.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AddressRepository extends JpaRepository<Address, UUID> {
}
