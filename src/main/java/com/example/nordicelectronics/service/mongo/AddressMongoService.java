package com.example.nordicelectronics.service.mongo;

import com.example.nordicelectronics.document.AddressDocument;
import com.example.nordicelectronics.repositories.mongodb.AddressMongoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AddressMongoService {

    private final AddressMongoRepository addressMongoRepository;

    public List<AddressDocument> getAll() {
        return addressMongoRepository.findAll();
    }

    public AddressDocument getById(String id) {
        return addressMongoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Address not found with id: " + id));
    }

    public AddressDocument getByUserId(String userId) {
        return addressMongoRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Address not found for user: " + userId));
    }

    public AddressDocument save(AddressDocument address) {
        return addressMongoRepository.save(address);
    }

    public AddressDocument update(String id, AddressDocument address) {
        AddressDocument existing = getById(id);
        existing.setStreet(address.getStreet());
        existing.setStreetNumber(address.getStreetNumber());
        existing.setZip(address.getZip());
        existing.setCity(address.getCity());
        return addressMongoRepository.save(existing);
    }

    public void deleteById(String id) {
        addressMongoRepository.deleteById(id);
    }

    public void deleteByUserId(String userId) {
        addressMongoRepository.deleteByUserId(userId);
    }


    public Integer nothingButATest() {
        return 1;
    }
}

