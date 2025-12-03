package com.example.nordicelectronics.service.neo4j;

import com.example.nordicelectronics.entity.neo4j.UserNode;
import com.example.nordicelectronics.repositories.neo4j.UserNeo4jRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserNeo4jService {

    private final UserNeo4jRepository userNeo4jRepository;

    public List<UserNode> getAll() {
        return userNeo4jRepository.findAll();
    }

    public UserNode getByUserId(UUID userId) {
        return userNeo4jRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
    }

    public UserNode getByEmail(String email) {
        return userNeo4jRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
    }

    public UserNode save(UserNode userNode) {
        if (userNode.getUserId() == null) {
            userNode.setUserId(UUID.randomUUID());
        }
        return userNeo4jRepository.save(userNode);
    }

    public UserNode update(UUID userId, UserNode userNode) {
        UserNode existing = getByUserId(userId);

        existing.setFirstName(userNode.getFirstName());
        existing.setLastName(userNode.getLastName());
        existing.setEmail(userNode.getEmail());
        existing.setPhoneNumber(userNode.getPhoneNumber());
        existing.setDateOfBirth(userNode.getDateOfBirth());
        existing.setPassword(userNode.getPassword());
        existing.setAdmin(userNode.isAdmin());

        return userNeo4jRepository.save(existing);
    }

    public void deleteByUserId(UUID userId) {
        userNeo4jRepository.deleteByUserId(userId);
    }
}

