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
        return addressRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("Address not found for user"));
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

        if (addressRepository.existsByUser_UserId(user.getUserId())) {
            throw new IllegalStateException("User already has an address.");
        }
        
        address.setUser(user);
        return addressRepository.save(address);
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
        addressRepository.deleteById(id);
    }

    public void deleteForUser(String email) {
        User user = userService.findByEmail(email);
        Address existing = getByUserId(user.getUserId());
        deleteById(existing.getAddressId());
    }


}
