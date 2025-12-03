package com.example.nordicelectronics.service;

import com.example.nordicelectronics.entity.Address;
import com.example.nordicelectronics.entity.User;
import com.example.nordicelectronics.repositories.sql.AddressRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
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

        if (user.getAddress() == null) {
            throw new EntityNotFoundException("User does not have an address");
        }

        return getById(user.getAddress().stream().findFirst().get().getAddressId());
    }

    public Address getByUserEmail(String email) {
        User user = userService.findByEmail(email);
        return getByUserId(user.getUserId());
    }

    public Address save(Address address) {
        return addressRepository.save(address);
    }

    public Address saveForUser(String email, Address address) {
        User user = userService.findByEmail(email);

        if (user.getAddress() != null) {
            throw new IllegalStateException("User already has an address.");
        }

        Address saved = addressRepository.save(address);

        user.setAddress(java.util.Collections.singletonList(saved));
        userService.save(user);

        return saved;
    }

    public Address update(UUID id, Address address) {
        Address existing = getById(id);

        existing.setStreet(address.getStreet());
        existing.setStreetNumber(address.getStreetNumber());
        existing.setCity(address.getCity());
        existing.setZip(address.getZip());
        existing.setCity(address.getCity());

        return addressRepository.save(existing);
    }

    public Address updateForUser(String email, Address address) {
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
