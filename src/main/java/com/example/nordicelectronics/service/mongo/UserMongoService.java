package com.example.nordicelectronics.service.mongo;

import com.example.nordicelectronics.document.UserDocument;
import com.example.nordicelectronics.repositories.mongodb.UserMongoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserMongoService {

    private final UserMongoRepository userMongoRepository;

    public List<UserDocument> getAll() {
        return userMongoRepository.findAll();
    }

    public UserDocument getById(String id) {
        return userMongoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    public UserDocument save(UserDocument user) {
        return userMongoRepository.save(user);
    }

    public UserDocument update(String id, UserDocument user) {
        UserDocument existing = getById(id);
        existing.setEmail(user.getEmail());
        existing.setFirstName(user.getFirstName());
        existing.setLastName(user.getLastName());
        existing.setPhoneNumber(user.getPhoneNumber());
        existing.setDateOfBirth(user.getDateOfBirth());
        existing.setAdmin(user.isAdmin());
        
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            existing.setPassword(user.getPassword());
        }
        
        if (user.getAddress() != null) {
            existing.setAddress(user.getAddress());
        }
        
        return userMongoRepository.save(existing);
    }

    public void deleteById(String id) {
        userMongoRepository.deleteById(id);
    }

    public UserDocument getByEmail(String email) {
        return userMongoRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
    }
}

