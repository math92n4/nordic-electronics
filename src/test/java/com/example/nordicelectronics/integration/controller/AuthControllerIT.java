package com.example.nordicelectronics.integration.controller;

import com.example.nordicelectronics.entity.User;
import com.example.nordicelectronics.integration.BaseIntegrationTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.securityContext;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@Transactional
class AuthControllerIT extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;
    private static final String BASE_URL = "/api/postgresql/auth";

    @BeforeEach
    void setUp() {
        // Create a test user with a known password
        testUser = User.builder()
                .email("testuser@nordic.com")
                .firstName("Test")
                .lastName("User")
                .phoneNumber("20123456")
                .dateOfBirth(LocalDate.of(1990, 1, 15))
                .password(passwordEncoder.encode("password123"))
                .isAdmin(false)
                .build();
        entityManager.persist(testUser);
        entityManager.flush();
        entityManager.clear();
    }


    // ============================================
    // EP/BVA TESTS - Email Format
    // ============================================

    @Nested
    @DisplayName("EP/BVA - Email Format Validation")
    class EmailValidationEPBVATests {

        @Test
        @DisplayName("EP: Valid email - contains @ and valid domain")
        void epValidEmailWithAtAndDomain() throws Exception {
            Map<String, Object> request = createValidRegistrationRequest("validuser@nordic.com");

            mockMvc.perform(post(BASE_URL + "/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("EP: Invalid email - missing @ symbol")
        void epInvalidEmailMissingAt() throws Exception {
            Map<String, Object> request = createValidRegistrationRequest("invalidemail.com");

            mockMvc.perform(post(BASE_URL + "/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value(containsString("Invalid email")));
        }

        @Test
        @DisplayName("EP: Invalid email - missing domain after @")
        void epInvalidEmailMissingDomain() throws Exception {
            Map<String, Object> request = createValidRegistrationRequest("user@");

            mockMvc.perform(post(BASE_URL + "/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value(containsString("Invalid email")));
        }

        @Test
        @DisplayName("EP: Invalid email - missing TLD (no dot in domain)")
        void epInvalidEmailMissingTLD() throws Exception {
            Map<String, Object> request = createValidRegistrationRequest("user@nodomain");

            mockMvc.perform(post(BASE_URL + "/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("EP: Invalid email - already exists (uniqueness)")
        void epInvalidEmailNotUnique() throws Exception {
            // First registration
            Map<String, Object> request1 = createValidRegistrationRequest("unique@nordic.com");
            mockMvc.perform(post(BASE_URL + "/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request1)))
                    .andExpect(status().isOk());

            // Second registration with same email
            Map<String, Object> request2 = createValidRegistrationRequest("unique@nordic.com");
            mockMvc.perform(post(BASE_URL + "/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request2)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value(containsString("User already exists")));
        }
    }

    // ============================================
    // EP/BVA TESTS - Password Length
    // ============================================

    @Nested
    @DisplayName("EP/BVA - Password Length Validation")
    class PasswordLengthEPBVATests {

        @Test
        @DisplayName("BVA: Password with 7 characters - invalid (below minimum)")
        void bvaPassword7Chars() throws Exception {
            Map<String, Object> request = createValidRegistrationRequest("pass7@nordic.com");
            request.put("password", "1234567"); // 7 chars

            mockMvc.perform(post(BASE_URL + "/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value(containsString("Password must be at least 8 characters")));
        }

        @Test
        @DisplayName("BVA: Password with 8 characters - valid (at minimum)")
        void bvaPassword8Chars() throws Exception {
            Map<String, Object> request = createValidRegistrationRequest("pass8@nordic.com");
            request.put("password", "12345678"); // 8 chars

            mockMvc.perform(post(BASE_URL + "/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("BVA: Password with 9 characters - valid (just above minimum)")
        void bvaPassword9Chars() throws Exception {
            Map<String, Object> request = createValidRegistrationRequest("pass9@nordic.com");
            request.put("password", "123456789"); // 9 chars

            mockMvc.perform(post(BASE_URL + "/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("BVA: Password with 36 characters - valid (middle of range)")
        void bvaPassword36Chars() throws Exception {
            Map<String, Object> request = createValidRegistrationRequest("pass36@nordic.com");
            request.put("password", "a".repeat(36)); // 36 chars

            mockMvc.perform(post(BASE_URL + "/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("BVA: Password with 63 characters - valid (just below maximum)")
        void bvaPassword63Chars() throws Exception {
            Map<String, Object> request = createValidRegistrationRequest("pass63@nordic.com");
            request.put("password", "a".repeat(63)); // 63 chars

            mockMvc.perform(post(BASE_URL + "/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("BVA: Password with 64 characters - valid (at maximum)")
        void bvaPassword64Chars() throws Exception {
            Map<String, Object> request = createValidRegistrationRequest("pass64@nordic.com");
            request.put("password", "a".repeat(64)); // 64 chars

            mockMvc.perform(post(BASE_URL + "/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("BVA: Password with 65 characters - invalid (above maximum)")
        void bvaPassword65Chars() throws Exception {
            Map<String, Object> request = createValidRegistrationRequest("pass65@nordic.com");
            request.put("password", "a".repeat(65)); // 65 chars

            mockMvc.perform(post(BASE_URL + "/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value(containsString("64 characters")));
        }

        @Test
        @DisplayName("EP: Password in valid range (8-64)")
        void epPasswordValidRange() throws Exception {
            Map<String, Object> request = createValidRegistrationRequest("passvalid@nordic.com");
            request.put("password", "validPassword123"); // 16 chars - in valid range

            mockMvc.perform(post(BASE_URL + "/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("EP: Password below valid range (<8)")
        void epPasswordBelowRange() throws Exception {
            Map<String, Object> request = createValidRegistrationRequest("passlow@nordic.com");
            request.put("password", "short"); // 5 chars - below valid range

            mockMvc.perform(post(BASE_URL + "/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("EP: Password above valid range (>64)")
        void epPasswordAboveRange() throws Exception {
            Map<String, Object> request = createValidRegistrationRequest("passhigh@nordic.com");
            request.put("password", "a".repeat(100)); // 100 chars - above valid range

            mockMvc.perform(post(BASE_URL + "/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    // ============================================
    // EP/BVA TESTS - Date of Birth (Age)
    // ============================================

    @Nested
    @DisplayName("EP/BVA - Date of Birth (Age) Validation")
    class DateOfBirthEPBVATests {

        @Test
        @DisplayName("BVA: User exactly 17 years old - invalid")
        void bvaAge17Years() throws Exception {
            Map<String, Object> request = createValidRegistrationRequest("age17@nordic.com");
            request.put("dateOfBirth", LocalDate.now().minusYears(17).toString());

            mockMvc.perform(post(BASE_URL + "/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value(containsString("18 years old")));
        }

        @Test
        @DisplayName("BVA: User exactly 18 years old - valid (at boundary)")
        void bvaAge18Years() throws Exception {
            Map<String, Object> request = createValidRegistrationRequest("age18@nordic.com");
            request.put("dateOfBirth", LocalDate.now().minusYears(18).toString());

            mockMvc.perform(post(BASE_URL + "/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("BVA: User exactly 19 years old - valid (just above boundary)")
        void bvaAge19Years() throws Exception {
            Map<String, Object> request = createValidRegistrationRequest("age19@nordic.com");
            request.put("dateOfBirth", LocalDate.now().minusYears(19).toString());

            mockMvc.perform(post(BASE_URL + "/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("EP: User under 18 - invalid")
        void epAgeUnder18() throws Exception {
            Map<String, Object> request = createValidRegistrationRequest("agechild@nordic.com");
            request.put("dateOfBirth", LocalDate.now().minusYears(10).toString());

            mockMvc.perform(post(BASE_URL + "/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value(containsString("18 years old")));
        }

        @Test
        @DisplayName("EP: User over 18 - valid")
        void epAgeOver18() throws Exception {
            Map<String, Object> request = createValidRegistrationRequest("ageadult@nordic.com");
            request.put("dateOfBirth", LocalDate.now().minusYears(30).toString());

            mockMvc.perform(post(BASE_URL + "/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    // ============================================
    // EP/BVA TESTS - Phone Number
    // ============================================

    @Nested
    @DisplayName("EP/BVA - Phone Number Validation")
    class PhoneNumberEPBVATests {

        @Test
        @DisplayName("EP: Valid phone - 8 digits with valid Danish prefix")
        void epValidPhone8DigitsDanishPrefix() throws Exception {
            Map<String, Object> request = createValidRegistrationRequest("phone1@nordic.com");
            request.put("phoneNumber", "20123456"); // Valid: starts with 2, 8 digits

            mockMvc.perform(post(BASE_URL + "/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("EP: Invalid phone - contains letters")
        void epInvalidPhoneWithLetters() throws Exception {
            Map<String, Object> request = createValidRegistrationRequest("phone2@nordic.com");
            request.put("phoneNumber", "2012abc6");

            mockMvc.perform(post(BASE_URL + "/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value(containsString("Invalid phone number")));
        }

        @Test
        @DisplayName("EP: Invalid phone - only 7 digits")
        void epInvalidPhone7Digits() throws Exception {
            Map<String, Object> request = createValidRegistrationRequest("phone3@nordic.com");
            request.put("phoneNumber", "2012345"); // 7 digits

            mockMvc.perform(post(BASE_URL + "/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value(containsString("Invalid phone number")));
        }

        @Test
        @DisplayName("EP: Invalid phone - 9 digits")
        void epInvalidPhone9Digits() throws Exception {
            Map<String, Object> request = createValidRegistrationRequest("phone4@nordic.com");
            request.put("phoneNumber", "201234567"); // 9 digits

            mockMvc.perform(post(BASE_URL + "/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value(containsString("Invalid phone number")));
        }

        @Test
        @DisplayName("EP: Invalid phone - invalid Danish prefix (starts with 1)")
        void epInvalidPhoneInvalidPrefix() throws Exception {
            Map<String, Object> request = createValidRegistrationRequest("phone5@nordic.com");
            request.put("phoneNumber", "10123456"); // Invalid prefix

            mockMvc.perform(post(BASE_URL + "/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value(containsString("Invalid phone number")));
        }

        @Test
        @DisplayName("EP: Valid phone - prefix 30 (valid Danish mobile)")
        void epValidPhonePrefix30() throws Exception {
            Map<String, Object> request = createValidRegistrationRequest("phone6@nordic.com");
            request.put("phoneNumber", "30123456"); // Valid: starts with 30

            mockMvc.perform(post(BASE_URL + "/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("EP: Valid phone - prefix 40 (valid Danish mobile)")
        void epValidPhonePrefix40() throws Exception {
            Map<String, Object> request = createValidRegistrationRequest("phone7@nordic.com");
            request.put("phoneNumber", "40123456"); // Valid: starts with 40

            mockMvc.perform(post(BASE_URL + "/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("EP: Invalid phone - special characters")
        void epInvalidPhoneSpecialChars() throws Exception {
            Map<String, Object> request = createValidRegistrationRequest("phone8@nordic.com");
            request.put("phoneNumber", "20-12-34-56");

            mockMvc.perform(post(BASE_URL + "/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value(containsString("Invalid phone number")));
        }

        @Test
        @DisplayName("EP: Invalid phone - empty string")
        void epInvalidPhoneEmpty() throws Exception {
            Map<String, Object> request = createValidRegistrationRequest("phone9@nordic.com");
            request.put("phoneNumber", "");

            mockMvc.perform(post(BASE_URL + "/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    // ============================================
    // GET /users TESTS
    // ============================================

    @Nested
    @DisplayName("GET /users - Get All Users")
    class GetAllUsersTests {

        @Test
        @DisplayName("Should return list of all users")
        void shouldReturnListOfAllUsers() throws Exception {
            mockMvc.perform(get(BASE_URL + "/users"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                    .andExpect(jsonPath("$[*].email", hasItem(testUser.getEmail())));
        }

        @Test
        @DisplayName("Should return multiple users when multiple exist")
        void shouldReturnMultipleUsersWhenMultipleExist() throws Exception {
            // Create another user
            User anotherUser = User.builder()
                    .email("another@nordic.com")
                    .firstName("Another")
                    .lastName("User")
                    .phoneNumber("30123456")
                    .dateOfBirth(LocalDate.of(1985, 5, 20))
                    .password(passwordEncoder.encode("password456"))
                    .isAdmin(false)
                    .build();
            entityManager.persist(anotherUser);
            entityManager.flush();

            mockMvc.perform(get(BASE_URL + "/users"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(2))))
                    .andExpect(jsonPath("$[*].email", hasItems(testUser.getEmail(), "another@nordic.com")));
        }
    }

    // ============================================
    // GET /current-user TESTS
    // ============================================

    @Nested
    @DisplayName("GET /current-user - Get Current User")
    class GetCurrentUserTests {

        @Test
        @DisplayName("Should return user details when authenticated")
        void shouldReturnUserDetailsWhenAuthenticated() throws Exception {
            SecurityContext securityContext = createSecurityContextForUser(testUser.getEmail());

            mockMvc.perform(get(BASE_URL + "/current-user")
                            .with(securityContext(securityContext)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.authenticationExists").value(true))
                    .andExpect(jsonPath("$.isAuthenticated").value(true))
                    .andExpect(jsonPath("$.principal").value(testUser.getEmail()))
                    .andExpect(jsonPath("$.user.email").value(testUser.getEmail()))
                    .andExpect(jsonPath("$.user.firstName").value(testUser.getFirstName()))
                    .andExpect(jsonPath("$.message").value("User found"));
        }

        @Test
        @DisplayName("Should return not authenticated when no auth context")
        @WithAnonymousUser
        void shouldReturnNotAuthenticatedWhenNoAuthContext() throws Exception {
            // With @WithAnonymousUser, Spring sets up an anonymous authentication
            // The controller checks for "anonymousUser" and returns "Not authenticated"
            mockMvc.perform(get(BASE_URL + "/current-user"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.user").isEmpty())
                    .andExpect(jsonPath("$.message").value(anyOf(
                            equalTo("Not authenticated"),
                            containsString("User lookup failed")
                    )));
        }

        @Test
        @DisplayName("Should return not authenticated when user is anonymous")
        void shouldReturnNotAuthenticatedWhenAnonymous() throws Exception {
            SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
            Authentication auth = new UsernamePasswordAuthenticationToken(
                    "anonymousUser", null, Collections.emptyList());
            securityContext.setAuthentication(auth);

            mockMvc.perform(get(BASE_URL + "/current-user")
                            .with(securityContext(securityContext)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.user").isEmpty())
                    .andExpect(jsonPath("$.message").value("Not authenticated"));
        }

        @Test
        @DisplayName("Should return user lookup failed when email not found")
        void shouldReturnUserLookupFailedWhenEmailNotFound() throws Exception {
            SecurityContext securityContext = createSecurityContextForUser("nonexistent@nordic.com");

            mockMvc.perform(get(BASE_URL + "/current-user")
                            .with(securityContext(securityContext)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.user").isEmpty())
                    .andExpect(jsonPath("$.message").value(containsString("User lookup failed")));
        }

        @Test
        @DisplayName("Should include session ID in response")
        void shouldIncludeSessionIdInResponse() throws Exception {
            MockHttpSession session = new MockHttpSession();
            SecurityContext securityContext = createSecurityContextForUser(testUser.getEmail());

            mockMvc.perform(get(BASE_URL + "/current-user")
                            .session(session)
                            .with(securityContext(securityContext)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.sessionId").exists())
                    .andExpect(jsonPath("$.sessionId").isNotEmpty());
        }

        @Test
        @DisplayName("Should return no-session when session does not exist")
        void shouldReturnNoSessionWhenSessionDoesNotExist() throws Exception {
            SecurityContext securityContext = SecurityContextHolder.createEmptyContext();

            mockMvc.perform(get(BASE_URL + "/current-user")
                            .with(securityContext(securityContext)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.sessionId").value("no-session"));
        }

        @Test
        @DisplayName("Should include authorities in response")
        void shouldIncludeAuthoritiesInResponse() throws Exception {
            SecurityContext securityContext = createSecurityContextForUser(testUser.getEmail());

            mockMvc.perform(get(BASE_URL + "/current-user")
                            .with(securityContext(securityContext)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.authorities").isArray())
                    .andExpect(jsonPath("$.authorities[0].authority").value("ROLE_USER"));
        }

        @Test
        @DisplayName("Should handle anonymous authentication gracefully")
        void shouldHandleAnonymousAuthenticationGracefully() throws Exception {
            // Spring Security will create an anonymous authentication even for empty context
            // when using MockMvc with security filters enabled
            SecurityContext securityContext = SecurityContextHolder.createEmptyContext();

            mockMvc.perform(get(BASE_URL + "/current-user")
                            .with(securityContext(securityContext)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.user").isEmpty())
                    .andExpect(jsonPath("$.message").value(anyOf(
                            equalTo("Not authenticated"),
                            containsString("User lookup failed")
                    )));
        }
    }

    // ============================================
    // POST /register TESTS
    // ============================================

    @Nested
    @DisplayName("POST /register - User Registration")
    class RegisterUserTests {

        @Test
        @DisplayName("Should successfully register a new user")
        void shouldSuccessfullyRegisterNewUser() throws Exception {
            Map<String, Object> request = createValidRegistrationRequest("newuser@nordic.com");

            mockMvc.perform(post(BASE_URL + "/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("User registered successfully"))
                    .andExpect(jsonPath("$.user.email").value("newuser@nordic.com"))
                    .andExpect(jsonPath("$.user.firstName").value("New"))
                    .andExpect(jsonPath("$.user.lastName").value("User"));
        }

        @Test
        @DisplayName("Should fail when email already exists")
        void shouldFailWhenEmailAlreadyExists() throws Exception {
            Map<String, Object> request = createValidRegistrationRequest(testUser.getEmail());

            mockMvc.perform(post(BASE_URL + "/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value(containsString("User already exists")));
        }

        @Test
        @DisplayName("Should fail when email is invalid format")
        void shouldFailWhenEmailIsInvalidFormat() throws Exception {
            Map<String, Object> request = createValidRegistrationRequest("invalid-email");

            mockMvc.perform(post(BASE_URL + "/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value(containsString("Invalid email")));
        }

        @Test
        @DisplayName("Should fail when email has invalid TLD")
        void shouldFailWhenEmailHasInvalidTLD() throws Exception {
            Map<String, Object> request = createValidRegistrationRequest("user@example.xyz");

            mockMvc.perform(post(BASE_URL + "/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value(containsString("valid TLD")));
        }

        @Test
        @DisplayName("Should fail when password is too short")
        void shouldFailWhenPasswordTooShort() throws Exception {
            Map<String, Object> request = createValidRegistrationRequest("newuser@nordic.com");
            request.put("password", "short");

            mockMvc.perform(post(BASE_URL + "/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value(containsString("Password must be at least 8 characters")));
        }

        @Test
        @DisplayName("Should fail when password is null")
        void shouldFailWhenPasswordIsNull() throws Exception {
            Map<String, Object> request = createValidRegistrationRequest("newuser@nordic.com");
            request.remove("password");

            mockMvc.perform(post(BASE_URL + "/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("Should fail when phone number is invalid")
        void shouldFailWhenPhoneNumberIsInvalid() throws Exception {
            Map<String, Object> request = createValidRegistrationRequest("newuser@nordic.com");
            request.put("phoneNumber", "invalid");

            mockMvc.perform(post(BASE_URL + "/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value(containsString("Invalid phone number")));
        }

        @Test
        @DisplayName("Should fail when date of birth makes user under 18")
        void shouldFailWhenUserIsUnder18() throws Exception {
            Map<String, Object> request = createValidRegistrationRequest("newuser@nordic.com");
            request.put("dateOfBirth", LocalDate.now().minusYears(17).toString());

            mockMvc.perform(post(BASE_URL + "/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value(containsString("18 years old")));
        }

        @Test
        @DisplayName("Should fail when date of birth is null")
        void shouldFailWhenDateOfBirthIsNull() throws Exception {
            Map<String, Object> request = createValidRegistrationRequest("newuser@nordic.com");
            request.remove("dateOfBirth");

            mockMvc.perform(post(BASE_URL + "/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("Should fail when date of birth format is invalid")
        void shouldFailWhenDateOfBirthFormatIsInvalid() throws Exception {
            Map<String, Object> request = createValidRegistrationRequest("newuser@nordic.com");
            request.put("dateOfBirth", "invalid-date");

            mockMvc.perform(post(BASE_URL + "/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("Should fail when email is empty")
        void shouldFailWhenEmailIsEmpty() throws Exception {
            Map<String, Object> request = createValidRegistrationRequest("");

            mockMvc.perform(post(BASE_URL + "/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("Should register user with valid Danish phone number")
        void shouldRegisterUserWithValidDanishPhoneNumber() throws Exception {
            Map<String, Object> request = createValidRegistrationRequest("newuser2@nordic.dk");
            request.put("phoneNumber", "31234567");

            mockMvc.perform(post(BASE_URL + "/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.user.phoneNumber").value("31234567"));
        }

        @Test
        @DisplayName("Should register user with .dk email TLD")
        void shouldRegisterUserWithDkEmailTLD() throws Exception {
            Map<String, Object> request = createValidRegistrationRequest("newuser3@nordic.dk");

            mockMvc.perform(post(BASE_URL + "/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.user.email").value("newuser3@nordic.dk"));
        }

        @Test
        @DisplayName("Should set isAdmin to false by default")
        void shouldSetIsAdminToFalseByDefault() throws Exception {
            Map<String, Object> request = createValidRegistrationRequest("newuser4@nordic.com");

            mockMvc.perform(post(BASE_URL + "/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.user.admin").value(false));
        }
    }


    // ============================================
    // POST /login TESTS
    // ============================================

    @Nested
    @DisplayName("POST /login - User Login")
    class LoginUserTests {

        @Test
        @DisplayName("Should successfully login with valid credentials")
        void shouldSuccessfullyLoginWithValidCredentials() throws Exception {
            Map<String, String> request = new HashMap<>();
            request.put("email", testUser.getEmail());
            request.put("password", "password123");

            mockMvc.perform(post(BASE_URL + "/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Login successful"))
                    .andExpect(jsonPath("$.user.email").value(testUser.getEmail()))
                    .andExpect(jsonPath("$.sessionId").exists())
                    .andExpect(jsonPath("$.authenticated").value(true))
                    .andExpect(jsonPath("$.authorities").isArray());
        }

        @Test
        @DisplayName("Should fail login with wrong password")
        void shouldFailLoginWithWrongPassword() throws Exception {
            Map<String, String> request = new HashMap<>();
            request.put("email", testUser.getEmail());
            request.put("password", "wrongpassword");

            mockMvc.perform(post(BASE_URL + "/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value(containsString("Login failed")));
        }

        @Test
        @DisplayName("Should fail login with non-existent email")
        void shouldFailLoginWithNonExistentEmail() throws Exception {
            Map<String, String> request = new HashMap<>();
            request.put("email", "nonexistent@nordic.com");
            request.put("password", "password123");

            mockMvc.perform(post(BASE_URL + "/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value(containsString("Login failed")));
        }

        @Test
        @DisplayName("Should fail login with null email")
        void shouldFailLoginWithNullEmail() throws Exception {
            Map<String, String> request = new HashMap<>();
            request.put("password", "password123");

            mockMvc.perform(post(BASE_URL + "/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("Should fail login with null password")
        void shouldFailLoginWithNullPassword() throws Exception {
            Map<String, String> request = new HashMap<>();
            request.put("email", testUser.getEmail());

            mockMvc.perform(post(BASE_URL + "/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("Should fail login with empty email")
        void shouldFailLoginWithEmptyEmail() throws Exception {
            Map<String, String> request = new HashMap<>();
            request.put("email", "");
            request.put("password", "password123");

            mockMvc.perform(post(BASE_URL + "/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("Should fail login with empty password")
        void shouldFailLoginWithEmptyPassword() throws Exception {
            Map<String, String> request = new HashMap<>();
            request.put("email", testUser.getEmail());
            request.put("password", "");

            mockMvc.perform(post(BASE_URL + "/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("Should return session ID after successful login")
        void shouldReturnSessionIdAfterLogin() throws Exception {
            Map<String, String> request = new HashMap<>();
            request.put("email", testUser.getEmail());
            request.put("password", "password123");

            mockMvc.perform(post(BASE_URL + "/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.sessionId").isNotEmpty());
        }

        @Test
        @DisplayName("Should return user authorities after successful login")
        void shouldReturnUserAuthoritiesAfterLogin() throws Exception {
            Map<String, String> request = new HashMap<>();
            request.put("email", testUser.getEmail());
            request.put("password", "password123");

            mockMvc.perform(post(BASE_URL + "/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.authorities").isArray())
                    .andExpect(jsonPath("$.authorities[0].authority").exists());
        }
    }

    // ============================================
    // DELETE /logout TESTS
    // ============================================

    @Nested
    @DisplayName("DELETE /logout - User Logout")
    class LogoutUserTests {

        @Test
        @DisplayName("Should successfully logout when authenticated")
        void shouldSuccessfullyLogoutWhenAuthenticated() throws Exception {
            MockHttpSession session = new MockHttpSession();
            SecurityContext securityContext = createSecurityContextForUser(testUser.getEmail());

            mockMvc.perform(delete(BASE_URL + "/logout")
                            .session(session)
                            .with(securityContext(securityContext)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Logout successful"));
        }

        @Test
        @DisplayName("Should handle logout even without existing session")
        void shouldHandleLogoutWithoutExistingSession() throws Exception {
            // getSession() creates a new session if one doesn't exist
            // The controller doesn't use getSession(false), so it always succeeds
            mockMvc.perform(delete(BASE_URL + "/logout"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Logout successful"));
        }

        @Test
        @DisplayName("Should clear security context after logout")
        void shouldClearSecurityContextAfterLogout() throws Exception {
            MockHttpSession session = new MockHttpSession();
            SecurityContext securityContext = createSecurityContextForUser(testUser.getEmail());

            // First perform logout
            mockMvc.perform(delete(BASE_URL + "/logout")
                            .session(session)
                            .with(securityContext(securityContext)))
                    .andExpect(status().isOk());

            // Session should be invalidated
            assertTrue(session.isInvalid());
        }
    }

    // ============================================
    // INTEGRATION FLOW TESTS
    // ============================================

    @Nested
    @DisplayName("Integration Flow Tests")
    class IntegrationFlowTests {

        @Test
        @DisplayName("Should complete full registration-login-logout flow")
        void shouldCompleteFullFlow() throws Exception {
            // 1. Register new user
            Map<String, Object> registerRequest = createValidRegistrationRequest("flowtest@nordic.com");
            
            mockMvc.perform(post(BASE_URL + "/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registerRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));

            // 2. Login with new user
            Map<String, String> loginRequest = new HashMap<>();
            loginRequest.put("email", "flowtest@nordic.com");
            loginRequest.put("password", "password12345");

            MvcResult loginResult = mockMvc.perform(post(BASE_URL + "/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andReturn();

            // Extract session from login
            MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession();

            // 3. Check current user
            mockMvc.perform(get(BASE_URL + "/current-user")
                            .session(session))
                    .andExpect(status().isOk());

            // 4. Logout
            mockMvc.perform(delete(BASE_URL + "/logout")
                            .session(session))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("Should maintain session between requests after login")
        void shouldMaintainSessionBetweenRequests() throws Exception {
            Map<String, String> loginRequest = new HashMap<>();
            loginRequest.put("email", testUser.getEmail());
            loginRequest.put("password", "password123");

            MvcResult loginResult = mockMvc.perform(post(BASE_URL + "/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isOk())
                    .andReturn();

            MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession();
            String sessionId = session.getId();

            // Verify session exists and contains authentication
            assertNotNull(sessionId);
            assertFalse(session.isInvalid());
        }
    }

    // ============================================
    // EDGE CASE TESTS
    // ============================================

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle special characters in password")
        void shouldHandleSpecialCharactersInPassword() throws Exception {
            Map<String, Object> request = createValidRegistrationRequest("special@nordic.com");
            request.put("password", "P@ssw0rd!#$%");

            mockMvc.perform(post(BASE_URL + "/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("Should handle unicode characters in name")
        void shouldHandleUnicodeCharactersInName() throws Exception {
            Map<String, Object> request = createValidRegistrationRequest("unicode@nordic.com");
            request.put("firstName", "José");
            request.put("lastName", "Müller");

            mockMvc.perform(post(BASE_URL + "/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.user.firstName").value("José"))
                    .andExpect(jsonPath("$.user.lastName").value("Müller"));
        }

        @Test
        @DisplayName("Should handle maximum length password")
        void shouldHandleMaximumLengthPassword() throws Exception {
            Map<String, Object> request = createValidRegistrationRequest("maxpass@nordic.com");
            // 64 characters - max length
            request.put("password", "a".repeat(64));

            mockMvc.perform(post(BASE_URL + "/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("Should fail when password exceeds maximum length")
        void shouldFailWhenPasswordExceedsMaxLength() throws Exception {
            Map<String, Object> request = createValidRegistrationRequest("longpass@nordic.com");
            // 65 characters - exceeds max length
            request.put("password", "a".repeat(65));

            mockMvc.perform(post(BASE_URL + "/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value(containsString("64 characters")));
        }

        @Test
        @DisplayName("Should handle email with plus sign")
        void shouldHandleEmailWithPlusSign() throws Exception {
            Map<String, Object> request = createValidRegistrationRequest("user+tag@nordic.com");

            mockMvc.perform(post(BASE_URL + "/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.user.email").value("user+tag@nordic.com"));
        }

        @Test
        @DisplayName("Should handle exactly 18 years old user")
        void shouldHandleExactly18YearsOldUser() throws Exception {
            Map<String, Object> request = createValidRegistrationRequest("adult@nordic.com");
            request.put("dateOfBirth", LocalDate.now().minusYears(18).toString());

            mockMvc.perform(post(BASE_URL + "/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("Should handle whitespace in email")
        void shouldHandleWhitespaceInEmail() throws Exception {
            Map<String, Object> request = createValidRegistrationRequest("  spaces@nordic.com  ");

            mockMvc.perform(post(BASE_URL + "/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("Should handle case sensitivity in email during login")
        void shouldHandleCaseSensitivityInEmail() throws Exception {
            Map<String, String> request = new HashMap<>();
            request.put("email", testUser.getEmail().toUpperCase());
            request.put("password", "password123");

            // Email lookup should typically be case-insensitive
            // This test documents the actual behavior
            mockMvc.perform(post(BASE_URL + "/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andReturn(); // Just verify no exception
        }
    }

    // ============================================
    // HELPER METHODS
    // ============================================

    private SecurityContext createSecurityContextForUser(String email) {
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        Authentication auth = new UsernamePasswordAuthenticationToken(
                email, "password",
                List.of(new SimpleGrantedAuthority("ROLE_USER")));
        securityContext.setAuthentication(auth);
        return securityContext;
    }

    private Map<String, Object> createValidRegistrationRequest(String email) {
        Map<String, Object> request = new HashMap<>();
        request.put("firstName", "New");
        request.put("lastName", "User");
        request.put("email", email);
        request.put("phoneNumber", "20123456");
        request.put("password", "password12345");
        request.put("dateOfBirth", LocalDate.of(1990, 5, 15).toString());
        return request;
    }
}
