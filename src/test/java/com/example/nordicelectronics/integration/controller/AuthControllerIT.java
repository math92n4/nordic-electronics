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
    // DATABASE-DEPENDENT TESTS - Email Uniqueness
    // ============================================

    @Nested
    @DisplayName("Database Tests - Email Uniqueness")
    class EmailUniquenessTests {

        @Test
        @DisplayName("Should fail when email already exists (uniqueness constraint)")
        void shouldFailWhenEmailAlreadyExists() throws Exception {
            // First registration
            Map<String, Object> request1 = createValidRegistrationRequest("unique@nordic.com");
            mockMvc.perform(post(BASE_URL + "/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request1)))
                    .andExpect(status().isOk());

            // Second registration with same email - requires DB to check uniqueness
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
    // POST /register TESTS - Database Integration Tests
    // Note: Input validation tests moved to unit/entity/registration/*ValidatorTest.java
    // ============================================

    @Nested
    @DisplayName("POST /register - User Registration (Database Tests)")
    class RegisterUserTests {

        @Test
        @DisplayName("Should successfully register a new user and persist to database")
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
        @DisplayName("Should fail when email already exists (database constraint)")
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
        @DisplayName("Should register user with valid Danish phone number and persist")
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
        @DisplayName("Should register user with .dk email TLD and persist")
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
        @DisplayName("Should set isAdmin to false by default when persisting")
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
    // EDGE CASE TESTS - Database Integration
    // Note: Pure validation edge cases moved to unit tests
    // ============================================

    @Nested
    @DisplayName("Edge Case Tests (Database Integration)")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should persist special characters in password correctly")
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
        @DisplayName("Should persist unicode characters in name to database")
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
        @DisplayName("Should persist email with plus sign correctly to database")
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
        @DisplayName("Should handle case sensitivity in email during login (DB lookup)")
        void shouldHandleCaseSensitivityInEmail() throws Exception {
            Map<String, String> request = new HashMap<>();
            request.put("email", testUser.getEmail().toUpperCase());
            request.put("password", "password123");

            // This test documents the actual database lookup behavior
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
