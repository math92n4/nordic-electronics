package com.example.nordicelectronics.unit.service;

import com.example.nordicelectronics.entity.User;
import com.example.nordicelectronics.repositories.sql.UserRepository;
import com.example.nordicelectronics.service.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void getAllUsers_ShouldReturnList() {
        List<User> users = List.of(new User());
        when(userRepository.findAll()).thenReturn(users);

        assertEquals(users, userService.getAllUsers());
        verify(userRepository).findAll();
    }

    @Test
    void save_ShouldReturnSavedUser() {
        User user = new User();
        when(userRepository.save(user)).thenReturn(user);

        assertEquals(user, userService.save(user));
        verify(userRepository).save(user);
    }

    @Test
    void findByEmail_ShouldReturnUser_WhenExists() {
        User user = new User();
        when(userRepository.findByEmail("test@example.com")).thenReturn(java.util.Optional.of(user));

        assertEquals(user, userService.findByEmail("test@example.com"));
    }

    @Test
    void findByEmail_ShouldThrow_WhenNotFound() {
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> userService.findByEmail("unknown@example.com"));
    }

    @Test
    void findById_ShouldReturnUser_WhenExists() {
        UUID id = UUID.randomUUID();
        User user = new User();
        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        assertEquals(user, userService.findById(id));
    }

    @Test
    void findById_ShouldThrow_WhenNotFound() {
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> userService.findById(id));
    }

    @Test
    void registerUser_ShouldSaveUser_WhenValidInput() {
        // Given
        String email = "john@example.com";
        String password = "StrongPass123!";
        String phone = "50223344";
        LocalDate dob = LocalDate.of(1995, 5, 10);

        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(passwordEncoder.encode(password)).thenReturn("encodedpassword");

        // When
        User user = userService.registerUser(
                "John", "Doe", email, phone, password, dob, false);

        // Then
        assertNotNull(user);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerUser_ShouldThrowException_WhenEmailAlreadyExists() {
        // Given
        when(userRepository.existsByEmail("taken@example.com")).thenReturn(true);

        // Expect
        assertThrows(RuntimeException.class, () -> userService.registerUser(
                "Jane", "Doe", "taken@example.com", "50223344", "StrongPass123!", LocalDate.of(1990, 1, 1), false
        ));
    }

    @Test
    void registerUser_ShouldFail_WhenPhoneIsInvalid() {
        assertThrows(IllegalArgumentException.class, () -> userService.registerUser(
                "Mads", "Winkler", "mads@example.com", "123", "StrongPass123!", LocalDate.of(1990, 1, 1), false
        ));
    }

    @Test
    void registerUser_ShouldFail_WhenUserIsUnder18() {
        // dateOfBirthValidator should reject this
        LocalDate tooYoung = LocalDate.now().minusYears(10);

        assertThrows(IllegalArgumentException.class, () -> userService.registerUser(
                "Kid", "Test", "kid@example.com", "50223344", "StrongPass123!", tooYoung, false
        ));
    }


}
