package com.example.nordicelectronics.unit.service;

import com.example.nordicelectronics.entity.User;
import com.example.nordicelectronics.repositories.sql.UserRepository;
import com.example.nordicelectronics.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;


    // GET ALL

    @Test
    void getAllUsers_ShouldReturnList() {
        User user = User.builder()
                .firstName("Mathias")
                .build();
        User user1 = User.builder()
                .firstName("Pia")
                .build();
        List<User> users = List.of(user, user1);

        when(userRepository.findAll()).thenReturn(users);

        assertEquals(users, userService.getAllUsers());
        verify(userRepository).findAll();
    }

    // REGISTER USER!

    @Test
    void save_ShouldReturnSavedUser() {
        String firstName = "Mathias";
        String lastName = "Wulff";
        String email = "test@example.com";
        String phone = "20345678";
        String password = "Password123";
        LocalDate dob = LocalDate.of(1998, 8, 29);

        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(passwordEncoder.encode(password)).thenReturn("encodedPassword");

        User savedUser = User.builder()
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .phoneNumber(phone)
                .password("encodedPassword")
                .dateOfBirth(dob)
                .isAdmin(false)
                .build();

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        User result = userService.registerUser(
                firstName, lastName, email, phone, password, dob, false
        );

        assertEquals(email, result.getEmail());
        assertEquals("encodedPassword", result.getPassword());
        assertEquals(firstName, result.getFirstName());
        assertEquals(lastName, result.getLastName());
        assertFalse(result.isAdmin());

        verify(userRepository).existsByEmail(email);
        verify(passwordEncoder).encode(password);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerUser_ShouldFail_WhenEmailIsInvalid() {
        assertThrows(IllegalArgumentException.class, () -> userService.registerUser(
                "Mads", "Winkler", "mads@example.jj", "123", "StrongPass123!", LocalDate.of(1990, 1, 1), false
        ));
    }

    @Test
    void registerUser_ShouldFail_WhenUserHasInvalidPassword() {
        assertThrows(IllegalArgumentException.class, () -> userService.registerUser(
                "Mads", "Winkler", "mads@example.com", "20345678", "Str", LocalDate.of(1990, 1, 1), false
        ));
    }

    @Test
    void registerUser_ShouldFail_WhenPhoneIsInvalid() {
        assertThrows(IllegalArgumentException.class, () -> userService.registerUser(
                "Mads", "Winkler", "mads@example.com", "2034", "StrongPass123!", LocalDate.of(1990, 1, 1), false
        ));
    }

    @Test
    void registerUser_ShouldFail_WhenUserIsUnder18() {
        assertThrows(IllegalArgumentException.class, () -> userService.registerUser(
                "Mads", "Winkler", "mads@example.com", "20345678", "StrongPass123!", LocalDate.now().minusYears(17), false
        ));
    }

    @Test
    void registerUser_ShouldThrowException_WhenEmailAlreadyExists() {
        when(userRepository.existsByEmail("taken@example.com")).thenReturn(true);

        assertThrows(RuntimeException.class, () -> userService.registerUser(
                "Jane", "Doe", "taken@example.com", "50223344", "StrongPass123!", LocalDate.of(1990, 1, 1), false
        ));
    }

    // FIND BY EMAIL
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

    // FIND BY ID
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


}
