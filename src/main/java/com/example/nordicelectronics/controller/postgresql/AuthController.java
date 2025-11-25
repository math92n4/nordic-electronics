package com.example.nordicelectronics.controller.postgresql;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.example.nordicelectronics.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.*;
import com.example.nordicelectronics.service.UserService;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Tag(name ="PostgreSQL Authentication Controller", description = "Handles user authentication and registration using PostgreSQL. ")
@RestController
@RequestMapping("/api/postgresql/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private SecurityContextRepository securityContextRepository;

    @Operation(summary = "Test PostgreSQL endpoint", description = "Returns session and authentication details for testing purposes")
    @GetMapping("/test")
    public String test(HttpServletRequest request, HttpServletResponse response) {
        var session = request.getSession(false);
        String sessionId = (session != null) ? session.getId() : "no-session";
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = (auth != null) ? auth.getName() : "anonymous";

        return "SessionID: " + sessionId + " | User: " + username + " | Request URI: " + request.getRequestURI()
                + " | Status: " + response.getStatus();
    }

    @Operation(summary = "Get all PostgreSQL users", description = "Fetches a list of all registered users")
    @GetMapping("/users")
    public List<User> getUsers() {
        return userService.getAllUsers();
    }

    @Operation(summary = "Get current authenticated PostgreSQL user", description = "Fetches details of the currently authenticated user")
    @GetMapping("/current-user")
    public ResponseEntity<?> getCurrentUser(HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Map<String, Object> response = new HashMap<>();

        response.put("sessionId", request.getSession(false) != null ? request.getSession().getId() : "no-session");
        response.put("authenticationExists", auth != null);
        response.put("isAuthenticated", auth != null && auth.isAuthenticated());
        response.put("principal", auth != null ? auth.getName() : "no-principal");
        response.put("authorities", auth != null ? auth.getAuthorities() : "no-authorities");

        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            response.put("user", null);
            response.put("message", "Not authenticated");
            return ResponseEntity.ok(response);
        }

        try {
            User user = userService.findByEmail(auth.getName());
            response.put("user", user);
            response.put("message", "User found");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("user", null);
            response.put("message", "User lookup failed: " + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    @Operation(
            summary = "Register a new PostgreSQL user",
            description = "Registers a new user with the provided details",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "Demo User",
                                            value = """
                                                    {
                                                      "firstName": "user",
                                                      "lastName": "user",
                                                      "email": "user@mail.com",
                                                      "phoneNumber": "12345678",
                                                      "password": "user"
                                                    }
                                                    """,
                                            description = "Default demo user for testing"
                                    ),
                                    @ExampleObject(
                                            name = "Admin User",
                                            value = """
                                                    {
                                                      "firstName": "admin",
                                                      "lastName": "admin",
                                                      "email": "admin@mail.com",
                                                      "phoneNumber": "87654321",
                                                      "password": "admin"
                                                    }
                                                    """,
                                            description = "Admin user example"
                                    )
                            }
                    )
            )
    )
    @PostMapping("/register")
    public ResponseEntity<?> register(@org.springframework.web.bind.annotation.RequestBody Map<String, Object> request) {
        try {
            String firstName = (String) request.get("firstName");
            String lastName = (String) request.get("lastName");
            String email = (String) request.get("email");
            String phoneNumber = (String) request.get("phoneNumber");
            String password = (String) request.get("password");

            boolean isAdmin = false;

            User user = userService.registerUser(firstName, lastName, email, phoneNumber, password, isAdmin);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "User registered successfully");
            response.put("user", user);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Registration failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @Operation(
            summary = "PostgreSQL user login",
            description = "Authenticates a user and establishes a session",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "Demo User Login",
                                            value = """
                                                    {
                                                      "email": "user@mail.com",
                                                      "password": "user"
                                                    }
                                                    """,
                                            description = "Login with demo user credentials"
                                    ),
                                    @ExampleObject(
                                            name = "Admin User Login",
                                            value = """
                                                    {
                                                      "email": "admin@mail.com",
                                                      "password": "admin"
                                                    }
                                                    """,
                                            description = "Login with admin credentials"
                                    )
                            }
                    )
            )
    )
    @PostMapping("/login")
    public ResponseEntity<?> login(@org.springframework.web.bind.annotation.RequestBody Map<String, String> request, HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        try {
            String email = request.get("email");
            String password = request.get("password");

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );

            // Create new SecurityContext and set the authentication
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);

            // Save the SecurityContext to session using SecurityContextRepository
            securityContextRepository.saveContext(context, httpRequest, httpResponse);

            User user = userService.findByEmail(email);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Login successful");
            response.put("user", user);
            response.put("sessionId", httpRequest.getSession().getId());
            response.put("authenticated", authentication.isAuthenticated());
            response.put("authorities", authentication.getAuthorities());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Login failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @Operation(summary = "PostgreSQL user logout", description = "Logs out the current user and invalidates the session")
    @DeleteMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        try {
            request.getSession().invalidate();
            SecurityContextHolder.clearContext();
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Logout successful");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Logout failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}

