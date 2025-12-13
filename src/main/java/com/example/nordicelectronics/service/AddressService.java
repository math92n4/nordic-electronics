package com.example.nordicelectronics.service;

import com.example.nordicelectronics.entity.Address;
import com.example.nordicelectronics.entity.User;
import com.example.nordicelectronics.entity.dto.address.AddressRequestDTO;
import com.example.nordicelectronics.repositories.sql.AddressRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AddressService {
    private final AddressRepository addressRepository;
    @Lazy
    private final UserService userService;

    public Address getById(UUID id) {
        return addressRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Address not found"));
    }

    public Address getByUserId(UUID userId) {

        User user = userService.findById(userId);

        if (user.getAddress() == null || user.getAddress().isEmpty()) {
            throw new EntityNotFoundException("User does not have an address");
        }

        return getById(user.getAddress().stream().findFirst().orElseThrow(() -> new EntityNotFoundException("User does not have an address")).getAddressId());
    }

    public Address getByUserEmail(String email) {
        User user = userService.findByEmail(email);
        return getByUserId(user.getUserId());
    }

    public Address save(Address address) {
        return addressRepository.save(address);
    }

    public Address saveForUser(String email, AddressRequestDTO address) {
        User user = userService.findByEmail(email);

        if (user.getAddress() != null && !user.getAddress().isEmpty()) {
            throw new IllegalStateException("User already has an address.");
        }

        Address saved = Address.builder()
                .city(address.getCity())
                .zip(address.getZip())
                .street(address.getStreet())
                .streetNumber(address.getStreetNumber())
                .user(user)
                .build();

        addressRepository.save(saved);

        // Don't replace the collection - modify the existing one to avoid Hibernate orphan removal issues
        if (user.getAddress() == null) {
            user.setAddress(new ArrayList<>());
        } else {
            user.getAddress().clear();
        }
        user.getAddress().add(saved);
        userService.save(user);

        return saved;
    }

    public Address update(UUID id, AddressRequestDTO address) {
        Address existing = getById(id);

        existing.setStreet(address.getStreet());
        existing.setStreetNumber(address.getStreetNumber());
        existing.setCity(address.getCity());
        existing.setZip(address.getZip());
        existing.setCity(address.getCity());

        return addressRepository.save(existing);
    }

    public Address updateForUser(String email, AddressRequestDTO address) {
        User user = userService.findByEmail(email);
        Address existing = getByUserId(user.getUserId());
        return update(existing.getAddressId(), address);
    }

    public void deleteById(UUID id) {
        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Address not found"));
        address.softDelete();
        addressRepository.save(address);
    }

    public void deleteForUser(String email) {
        User user = userService.findByEmail(email);
        Address existing = getByUserId(user.getUserId());
        existing.softDelete();
        addressRepository.save(existing);
    }
}
