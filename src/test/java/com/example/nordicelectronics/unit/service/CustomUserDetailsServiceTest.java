package com.example.nordicelectronics.unit.service;

import com.example.nordicelectronics.entity.User;
import com.example.nordicelectronics.repositories.sql.UserRepository;
import com.example.nordicelectronics.service.CustomUserDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    private User testUser;
    private User adminUser;
    private static final String TEST_EMAIL = "test@example.com";
    private static final String ADMIN_EMAIL = "admin@example.com";
    private static final String TEST_PASSWORD = "hashedPassword123";

    @BeforeEach
    void setUp() {
        testUser = User.builder()
            .userId(UUID.randomUUID())
            .firstName("John")
            .lastName("Doe")
            .email(TEST_EMAIL)
            .phoneNumber("12345678")
            .dateOfBirth(LocalDate.of(1990, 1, 15))
            .password(TEST_PASSWORD)
            .isAdmin(false)
            .build();

        adminUser = User.builder()
            .userId(UUID.randomUUID())
            .firstName("Admin")
            .lastName("User")
            .email(ADMIN_EMAIL)
            .phoneNumber("87654321")
            .dateOfBirth(LocalDate.of(1985, 5, 20))
            .password(TEST_PASSWORD)
            .isAdmin(true)
            .build();
    }

    // ===== LOAD USER BY USERNAME (EMAIL) TESTS =====

    @Test
    void loadUserByUsername_shouldReturnUserDetailsWithUserRole_whenUserExists() {
        // Arrange
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));

        // Act
        UserDetails result = customUserDetailsService.loadUserByUsername(TEST_EMAIL);

        // Assert
        assertNotNull(result);
        assertEquals(TEST_EMAIL, result.getUsername());
        assertEquals(TEST_PASSWORD, result.getPassword());
        assertTrue(result.getAuthorities().stream()
            .anyMatch(auth -> auth.getAuthority().equals("ROLE_USER")));
        assertFalse(result.getAuthorities().stream()
            .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN")));

        verify(userRepository).findByEmail(TEST_EMAIL);
    }

    @Test
    void loadUserByUsername_shouldReturnUserDetailsWithAdminRole_whenAdminUserExists() {
        // Arrange
        when(userRepository.findByEmail(ADMIN_EMAIL)).thenReturn(Optional.of(adminUser));

        // Act
        UserDetails result = customUserDetailsService.loadUserByUsername(ADMIN_EMAIL);

        // Assert
        assertNotNull(result);
        assertEquals(ADMIN_EMAIL, result.getUsername());
        assertEquals(TEST_PASSWORD, result.getPassword());
        assertTrue(result.getAuthorities().stream()
            .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN")));
        assertFalse(result.getAuthorities().stream()
            .anyMatch(auth -> auth.getAuthority().equals("ROLE_USER")));

        verify(userRepository).findByEmail(ADMIN_EMAIL);
    }

    @Test
    void loadUserByUsername_shouldThrowUsernameNotFoundException_whenUserNotFound() {
        // Arrange
        String nonExistentEmail = "blabla@example.com";
        when(userRepository.findByEmail(nonExistentEmail)).thenReturn(Optional.empty());

        // Act & Assert
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class,
            () -> customUserDetailsService.loadUserByUsername(nonExistentEmail));

        assertTrue(exception.getMessage().contains("User not found with email: " + nonExistentEmail));
        verify(userRepository).findByEmail(nonExistentEmail);
    }

    // ===== EDGE CASE TESTS =====

    @Test
    void loadUserByUsername_shouldHandleRepositoryException() {
        // Arrange
        when(userRepository.findByEmail(TEST_EMAIL))
            .thenThrow(new RuntimeException("Database connection failed"));

        // Act & Assert
        assertThrows(RuntimeException.class,
            () -> customUserDetailsService.loadUserByUsername(TEST_EMAIL));

        verify(userRepository).findByEmail(TEST_EMAIL);
    }
}

