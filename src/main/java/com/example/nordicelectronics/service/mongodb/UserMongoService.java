package com.example.nordicelectronics.service.mongodb;

import com.example.nordicelectronics.entity.mongodb.UserDocument;
import com.example.nordicelectronics.repositories.mongodb.UserMongoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@ConditionalOnProperty(name = "spring.data.mongodb.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class UserMongoService {

    private final UserMongoRepository userMongoRepository;

    public List<UserDocument> getAll() {
        return userMongoRepository.findAll();
    }

    public UserDocument getByUserId(UUID userId) {
        return userMongoRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
    }

    public UserDocument getByEmail(String email) {
        return userMongoRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
    }

    public UserDocument save(UserDocument userDocument) {
        if (userDocument.getUserId() == null) {
            userDocument.setUserId(UUID.randomUUID());
        }
        return userMongoRepository.save(userDocument);
    }

    public UserDocument update(UUID userId, UserDocument userDocument) {
        UserDocument existing = getByUserId(userId);
        
        existing.setFirstName(userDocument.getFirstName());
        existing.setLastName(userDocument.getLastName());
        existing.setEmail(userDocument.getEmail());
        existing.setPhoneNumber(userDocument.getPhoneNumber());
        existing.setDateOfBirth(userDocument.getDateOfBirth());
        existing.setPassword(userDocument.getPassword());
        existing.setAdmin(userDocument.isAdmin());
        existing.setAddresses(userDocument.getAddresses());
        existing.setOrderIds(userDocument.getOrderIds());

        return userMongoRepository.save(existing);
    }

    public void deleteByUserId(UUID userId) {
        userMongoRepository.deleteByUserId(userId);
    }
}
