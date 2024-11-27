package com.clubconnect.auth.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
import org.springframework.security.crypto.password.PasswordEncoder;

import com.clubconnect.auth.repository.UserRepository;

import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSaveUser() {
        // Mock password encoding
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");

        // Test method
        userService.saveUser("testUser", "test@example.com", "password123", "ROLE_USER", Set.of(1, 2));

        // Verify repository interaction
        verify(userRepository).saveUser(
                "testUser",
                "test@example.com",
                "encodedPassword",
                "ROLE_USER",
                Set.of(1, 2)
        );
    }

    @Test
    void testFindByUsername_UserExists() {
        // Mock user data
        Map<String, AttributeValue> userAttributes = Map.of(
                "username", AttributeValue.builder().s("testUser").build(),
                "email", AttributeValue.builder().s("test@example.com").build()
        );
        when(userRepository.findUserByUsername("testUser")).thenReturn(userAttributes);

        // Test method
        Optional<Map<String, AttributeValue>> result = userService.findByUsername("testUser");

        // Assertions
        assertTrue(result.isPresent());
        assertEquals("testUser", result.get().get("username").s());
    }

    @Test
    void testFindByUsername_UserNotFound() {
        // Mock no user found
        when(userRepository.findUserByUsername("unknownUser")).thenReturn(null);

        // Test method
        Optional<Map<String, AttributeValue>> result = userService.findByUsername("unknownUser");

        // Assertions
        assertFalse(result.isPresent());
    }

    @Test
    void testGetUserRole_UserExists() {
        // Mock user data with role
        Map<String, AttributeValue> userAttributes = Map.of(
                "role", AttributeValue.builder().s("ROLE_ADMIN").build()
        );
        when(userRepository.findUserByUsername("testUser")).thenReturn(userAttributes);

        // Test method
        Optional<String> role = userService.getUserRole("testUser");

        // Assertions
        assertTrue(role.isPresent());
        assertEquals("ROLE_ADMIN", role.get());
    }

    @Test
    void testAddEventToUser_UserExists() {
        // Mock user data
        Map<String, AttributeValue> userAttributes = Map.of(
                "attendedEvents", AttributeValue.builder().ns("1", "2").build()
        );
        when(userRepository.findUserByUsername("testUser")).thenReturn(userAttributes);

        // Test method
        userService.addEventToUser("testUser", 3);

        // Verify repository update
        verify(userRepository).updateAttendedEvents("testUser", Set.of(1, 2, 3));
    }

    @Test
    void testAddEventToUser_UserNotFound() {
        // Mock user not found
        when(userRepository.findUserByUsername("unknownUser")).thenReturn(null);

        // Test method and assert exception
        assertThrows(IllegalArgumentException.class, () -> {
            userService.addEventToUser("unknownUser", 3);
        });
    }

    @Test
    void testRemoveEventFromUser() {
        // Mock attended events
        Map<String, AttributeValue> userAttributes = Map.of(
                "attendedEvents", AttributeValue.builder().ns("1", "2", "3").build()
        );
        when(userRepository.findUserByUsername("testUser")).thenReturn(userAttributes);

        // Test method
        userService.removeEventFromUser("testUser", 2);

        // Verify repository update
        verify(userRepository).updateAttendedEvents("testUser", Set.of(1, 3));
    }

    @Test
    void testEmailExists() {
        // Mock email exists
        when(userRepository.findUserByEmail("test@example.com")).thenReturn(Map.of());

        // Test method
        boolean exists = userService.emailExists("test@example.com");

        // Assertions
        assertTrue(exists);
    }
}
