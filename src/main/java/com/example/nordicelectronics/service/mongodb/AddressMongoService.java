package com.example.nordicelectronics.service.mongodb;

import com.example.nordicelectronics.entity.mongodb.AddressDocument;
import com.example.nordicelectronics.repositories.mongodb.AddressMongoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@ConditionalOnProperty(name = "spring.data.mongodb.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class AddressMongoService {

    private final AddressMongoRepository addressMongoRepository;

    public List<AddressDocument> getAll() {
        return addressMongoRepository.findAll();
    }

    public AddressDocument getByAddressId(UUID addressId) {
        return addressMongoRepository.findByAddressId(addressId)
                .orElseThrow(() -> new RuntimeException("Address not found with ID: " + addressId));
    }

    public List<AddressDocument> getByUserId(UUID userId) {
        return addressMongoRepository.findByUserId(userId);
    }

    public AddressDocument save(AddressDocument addressDocument) {
        if (addressDocument.getAddressId() == null) {
            addressDocument.setAddressId(UUID.randomUUID());
        }
        return addressMongoRepository.save(addressDocument);
    }

    public AddressDocument update(UUID addressId, AddressDocument addressDocument) {
        AddressDocument existing = getByAddressId(addressId);
        
        existing.setUserId(addressDocument.getUserId());
        existing.setStreet(addressDocument.getStreet());
        existing.setStreetNumber(addressDocument.getStreetNumber());
        existing.setZip(addressDocument.getZip());
        existing.setCity(addressDocument.getCity());

        return addressMongoRepository.save(existing);
    }

    public void deleteByAddressId(UUID addressId) {
        addressMongoRepository.deleteByAddressId(addressId);
    }
}

