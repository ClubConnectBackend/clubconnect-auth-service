package com.clubconnect.auth.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.clubconnect.auth.repository.UserRepository;

import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testLoadUserByUsername_UserExistsWithRoles() {
        // Mock user data
        Map<String, AttributeValue> userAttributes = Map.of(
            "password", AttributeValue.builder().s("password123").build(),
            "roles", AttributeValue.builder().l(
                AttributeValue.builder().s("ROLE_USER").build(),
                AttributeValue.builder().s("ROLE_ADMIN").build()
            ).build()
        );

        when(userRepository.findUserByUsername("testUser")).thenReturn(userAttributes);

        // Test method
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("testUser");

        // Assertions
        assertNotNull(userDetails);
        assertEquals("testUser", userDetails.getUsername());
        assertEquals("password123", userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_USER")));
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN")));
    }

    @Test
    void testLoadUserByUsername_UserExistsWithoutRoles() {
        // Mock user data
        Map<String, AttributeValue> userAttributes = Map.of(
            "password", AttributeValue.builder().s("password123").build()
        );

        when(userRepository.findUserByUsername("testUser")).thenReturn(userAttributes);

        // Test method
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("testUser");

        // Assertions
        assertNotNull(userDetails);
        assertEquals("testUser", userDetails.getUsername());
        assertEquals("password123", userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_USER"))); // Default role
    }

    @Test
    void testLoadUserByUsername_UserNotFound() {
        // Mock no user found
        when(userRepository.findUserByUsername("unknownUser")).thenReturn(null);

        // Test method and assert exception
        assertThrows(UsernameNotFoundException.class, () -> {
            customUserDetailsService.loadUserByUsername("unknownUser");
        });
    }

    @Test
    void testLoadUserByUsername_UserWithEmptyAttributes() {
        // Mock empty user data
        when(userRepository.findUserByUsername("testUser")).thenReturn(Map.of());

        // Test method and assert exception
        assertThrows(UsernameNotFoundException.class, () -> {
            customUserDetailsService.loadUserByUsername("testUser");
        });
    }
}
