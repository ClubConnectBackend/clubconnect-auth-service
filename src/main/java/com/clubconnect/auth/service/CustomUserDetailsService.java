package com.clubconnect.auth.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.clubconnect.auth.repository.UserRepository;

import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Fetch user data from DynamoDB
        var userAttributes = userRepository.findUserByUsername(username);
        if (userAttributes == null || userAttributes.isEmpty()) {
            throw new UsernameNotFoundException("User not found with username: " + username);
        }

        // Extract user details
        String password = userAttributes.get("password").s();
        List<String> roles = userAttributes.get("roles").l().stream()
                .map(AttributeValue::s)
                .collect(Collectors.toList());

        // Convert roles to GrantedAuthority and return UserDetails
        return org.springframework.security.core.userdetails.User.builder()
                .username(username)
                .password(password)
                .authorities(roles.stream()
                        .map(role -> new org.springframework.security.core.authority.SimpleGrantedAuthority(role))
                        .collect(Collectors.toList()))
                .build();
    }
}
