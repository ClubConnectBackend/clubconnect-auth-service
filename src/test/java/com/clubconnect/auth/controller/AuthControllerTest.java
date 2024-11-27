package com.clubconnect.auth.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import com.clubconnect.auth.model.User;
import com.clubconnect.auth.service.UserService;
import com.clubconnect.auth.util.JwtUtil;

import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

class AuthControllerTest {

    @InjectMocks
    private AuthController authController;

    @Mock
    private UserService userService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testRegisterUser_Success() {
        User user = new User("testUser", "test@example.com", "password", "ROLE_USER", Set.of());

        when(userService.userExists("testUser")).thenReturn(false);
        when(userService.emailExists("test@example.com")).thenReturn(false);

        ResponseEntity<?> response = authController.registerUser(user);

        assertEquals(201, response.getStatusCodeValue());
        assertEquals("User registered successfully.", response.getBody());
        verify(userService, times(1)).saveUser("testUser", "test@example.com", "password", "ROLE_USER", Set.of());
    }

    @Test
    void testRegisterUser_UsernameExists() {
        User user = new User("testUser", "test@example.com", "password", "ROLE_USER", Set.of());

        when(userService.userExists("testUser")).thenReturn(true);

        ResponseEntity<?> response = authController.registerUser(user);

        assertEquals(409, response.getStatusCodeValue());
        assertEquals("User registration failed: Username already exists.", response.getBody());
    }

    @Test
    void testLogin_Success() {
        AuthController.LoginRequest loginRequest = new AuthController.LoginRequest();
        loginRequest.setUsername("testUser");
        loginRequest.setPassword("password");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mock(UsernamePasswordAuthenticationToken.class));
        when(jwtUtil.generateToken("testUser")).thenReturn("mockJwtToken");

        ResponseEntity<?> response = authController.createAuthenticationToken(loginRequest);

        assertEquals(200, response.getStatusCodeValue());
        AuthController.JwtResponse jwtResponse = (AuthController.JwtResponse) response.getBody();
        assertNotNull(jwtResponse);
        assertEquals("mockJwtToken", jwtResponse.getToken());
    }

    @Test
    void testLogin_Failure() {
        AuthController.LoginRequest loginRequest = new AuthController.LoginRequest();
        loginRequest.setUsername("testUser");
        loginRequest.setPassword("wrongPassword");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new RuntimeException("Invalid credentials"));

        ResponseEntity<?> response = authController.createAuthenticationToken(loginRequest);

        assertEquals(401, response.getStatusCodeValue());
        assertEquals("Invalid credentials.", response.getBody());
    }

    @Test
    void testGetUserEmailByUsername_Success() {
        when(userService.findByUsername("testUser"))
                .thenReturn(Optional.of(Map.of("email", AttributeValue.builder().s("test@example.com").build())));

        ResponseEntity<?> response = authController.getUserEmailByUsername("testUser");

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("test@example.com", response.getBody());
    }

    @Test
    void testGetUserEmailByUsername_UserNotFound() {
        when(userService.findByUsername("unknownUser")).thenReturn(Optional.empty());

        ResponseEntity<?> response = authController.getUserEmailByUsername("unknownUser");

        assertEquals(404, response.getStatusCodeValue());
        assertEquals("User not found.", response.getBody());
    }

    @Test
    void testAddAttendedEvent_Success() {
        when(userService.userExists("testUser")).thenReturn(true);

        ResponseEntity<?> response = authController.addAttendedEvent("testUser", 1);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Event added to user's attended events.", response.getBody());
        verify(userService, times(1)).addEventToUser("testUser", 1);
    }

    @Test
    void testAddAttendedEvent_UserNotFound() {
        when(userService.userExists("unknownUser")).thenReturn(false);

        ResponseEntity<?> response = authController.addAttendedEvent("unknownUser", 1);

        assertEquals(404, response.getStatusCodeValue());
        assertEquals("User not found.", response.getBody());
    }
}
