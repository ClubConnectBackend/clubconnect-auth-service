package com.clubconnect.auth.service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.clubconnect.auth.repository.UserRepository;

import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Save a user with the provided roles
     *
     * @param username  the username of the user
     * @param email     the email of the user
     * @param password  the password of the user
     * @param roles     the roles to assign to the user
     */
    public void saveUser(String username, String email, String password, String... roles) {
        String encodedPassword = passwordEncoder.encode(password);
        userRepository.saveUser(username, email, encodedPassword, Arrays.asList(roles));
    }

    /**
     * Retrieve a user by username
     *
     * @param username the username to search
     * @return Optional containing user data if found, otherwise empty
     */
    public Optional<Map<String, AttributeValue>> findByUsername(String username) {
        Map<String, AttributeValue> user = userRepository.findUserByUsername(username);
        return user != null && !user.isEmpty() ? Optional.of(user) : Optional.empty();
    }

    /**
     * Retrieve roles for a given user
     *
     * @param username the username to fetch roles for
     * @return Optional containing an array of roles, or empty if not found
     */
    public Optional<String[]> getUserRoles(String username) {
        Optional<Map<String, AttributeValue>> userOpt = findByUsername(username);
        if (userOpt.isPresent()) {
            AttributeValue rolesAttr = userOpt.get().get("roles");
            if (rolesAttr != null && rolesAttr.hasL()) {
                List<AttributeValue> roleList = rolesAttr.l();
                return Optional.of(roleList.stream()
                                           .map(AttributeValue::s)
                                           .toArray(String[]::new));
            }
        }
        return Optional.empty();
    }
}
