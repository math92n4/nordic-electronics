package com.example.nordicelectronics.service;

import com.example.nordicelectronics.entity.User;
import com.example.nordicelectronics.entity.validator.UserValidator.DanishPhoneValidator;
import com.example.nordicelectronics.entity.validator.UserValidator.DateOfBirthValidator;
import com.example.nordicelectronics.entity.validator.UserValidator.EmailValidator;
import com.example.nordicelectronics.entity.validator.UserValidator.PasswordValidator;
import com.example.nordicelectronics.repositories.sql.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User registerUser(String firstName, String lastName, String email,
                           String phoneNumber, String password, LocalDate dateOfBirth, boolean isAdmin) {

        validateUserSignup(email, password, phoneNumber, dateOfBirth);

        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("User already exists with email: " + email);
        }

        User user = User.builder()
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .phoneNumber(phoneNumber)
                .dateOfBirth(dateOfBirth)
                .password(passwordEncoder.encode(password))
                .isAdmin(isAdmin)
                .build();

        return userRepository.save(user);
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
    }

    public User findById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    public User save(User user) {
        return userRepository.save(user);
    }

    private void validateUserSignup(String email, String password, String phone, LocalDate dateOfBirth) {
        EmailValidator.validateEmail(email);
        PasswordValidator.validatePassword(password);
        if(!DanishPhoneValidator.isValidDanishMobile(phone)) {
            throw new IllegalArgumentException("Invalid phone number");
        }
        DateOfBirthValidator.validateDateOfBirth(dateOfBirth);
    }
}
