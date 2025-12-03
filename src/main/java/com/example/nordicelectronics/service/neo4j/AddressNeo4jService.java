package com.example.nordicelectronics.service.neo4j;

import com.example.nordicelectronics.entity.neo4j.AddressNode;
import com.example.nordicelectronics.repositories.neo4j.AddressNeo4jRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AddressNeo4jService {

    private final AddressNeo4jRepository addressNeo4jRepository;

    public List<AddressNode> getAll() {
        return addressNeo4jRepository.findAll();
    }

    public AddressNode getByAddressId(UUID addressId) {
        return addressNeo4jRepository.findByAddressId(addressId)
                .orElseThrow(() -> new RuntimeException("Address not found with ID: " + addressId));
    }

    public List<AddressNode> getByUserId(UUID userId) {
        return addressNeo4jRepository.findByUserId(userId);
    }

    public AddressNode save(AddressNode addressNode) {
        if (addressNode.getAddressId() == null) {
            addressNode.setAddressId(UUID.randomUUID());
        }
        return addressNeo4jRepository.save(addressNode);
    }

    public AddressNode update(UUID addressId, AddressNode addressNode) {
        AddressNode existing = getByAddressId(addressId);

        existing.setUserId(addressNode.getUserId());
        existing.setStreet(addressNode.getStreet());
        existing.setStreetNumber(addressNode.getStreetNumber());
        existing.setZip(addressNode.getZip());
        existing.setCity(addressNode.getCity());

        return addressNeo4jRepository.save(existing);
    }

    public void deleteByAddressId(UUID addressId) {
        addressNeo4jRepository.deleteByAddressId(addressId);
    }
}

