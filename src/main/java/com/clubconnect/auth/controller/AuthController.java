package com.clubconnect.auth.controller;

import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.clubconnect.auth.model.User;
import com.clubconnect.auth.service.UserService;
import com.clubconnect.auth.util.JwtUtil;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        try {
            if (userService.userExists(user.getUsername())) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("User registration failed: Username already exists.");
            }

            if (userService.emailExists(user.getEmail())) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("User registration failed: Email already exists.");
            }

            userService.saveUser(user.getUsername(), user.getEmail(), user.getPassword(), "ROLE_USER", new HashSet<>());
            return ResponseEntity.status(HttpStatus.CREATED).body("User registered successfully.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("User registration failed: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred.");
        }
    }

    @PostMapping("/register-admin")
    public ResponseEntity<?> registerAdmin(@RequestBody User user) {
        try {
            userService.saveUser(user.getUsername(), user.getEmail(), user.getPassword(), "ROLE_ADMIN", new HashSet<>());
            return ResponseEntity.status(HttpStatus.CREATED).body("Admin registered successfully.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Admin registration failed: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred.");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            final String jwt = jwtUtil.generateToken(loginRequest.getUsername());
            return ResponseEntity.ok(new JwtResponse(jwt));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials.");
        }
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(HttpServletRequest request) {
        try {
            String authorizationHeader = request.getHeader("Authorization");
            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                String refreshToken = authorizationHeader.substring(7);
                String username = jwtUtil.extractUsername(refreshToken);

                if (jwtUtil.validateToken(refreshToken, username)) {
                    String newToken = jwtUtil.generateToken(username);
                    return ResponseEntity.ok(new JwtResponse(newToken));
                }
            }
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid or expired refresh token.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error refreshing token: " + e.getMessage());
        }
    }

    @DeleteMapping("/remove-event/{username}/{eventId}")
    public ResponseEntity<?> removeAttendedEvent(@PathVariable String username, @PathVariable Integer eventId) {
        try {
            if (!userService.userExists(username)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
            }
            userService.removeEventFromUser(username, eventId);
            return ResponseEntity.ok("Event removed from user's attended events.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error removing event: " + e.getMessage());
        }
    }

    @PostMapping("/add-event/{username}/{eventId}")
    public ResponseEntity<?> addAttendedEvent(@PathVariable String username, @PathVariable Integer eventId) {
        try {
            System.out.println("Adding event. Username: " + username + ", Event ID: " + eventId);
            if (!userService.userExists(username)) {
                System.err.println("User not found: " + username);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
            }
            userService.addEventToUser(username, eventId);
            System.out.println("Event added successfully for user: " + username);
            return ResponseEntity.ok("Event added to user's attended events.");
        } catch (Exception e) {
            System.err.println("Error adding event: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error adding event: " + e.getMessage());
        }
    }


    @GetMapping("/events/{username}")
    public ResponseEntity<?> getAttendedEvents(@PathVariable String username) {
        try {
            if (!userService.userExists(username)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
            }
            Set<Integer> events = userService.getAttendedEvents(username).orElse(Set.of());
            return ResponseEntity.ok(events);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error retrieving events: " + e.getMessage());
        }
    }

    static class LoginRequest {
        private String username;
        private String password;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    static class JwtResponse {
        private String token;

        public JwtResponse(String token) {
            this.token = token;
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }
    }
}
