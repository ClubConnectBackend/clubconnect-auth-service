package com.clubconnect.auth.model;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

/**
 * Represents a User entity to be stored in DynamoDB.
 */
public class User {

    private String username;
    private String email;
    private String password;
    private String role; // Single role (e.g., ROLE_ADMIN or ROLE_USER)
    private Set<Integer> attendedEvents; // Set of event IDs the user is attending

    // Default constructor
    public User() {}

    // Parameterized constructor
    public User(String username, String email, String password, String role, Set<Integer> attendedEvents) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = role;
        this.attendedEvents = attendedEvents;
    }

    // Getters and setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Set<Integer> getAttendedEvents() {
        return attendedEvents;
    }

    public void setAttendedEvents(Set<Integer> attendedEvents) {
        this.attendedEvents = attendedEvents;
    }

    /**
     * Converts this User object to a DynamoDB attribute map.
     *
     * @return a map of AttributeValue for DynamoDB storage
     */
    public Map<String, AttributeValue> toDynamoDbMap() {
        return Map.of(
            "username", AttributeValue.builder().s(this.username).build(),
            "email", AttributeValue.builder().s(this.email).build(),
            "password", AttributeValue.builder().s(this.password).build(),
            "role", AttributeValue.builder().s(this.role).build(),
            "attendedEvents", AttributeValue.builder().ns(this.attendedEvents.stream()
                .map(String::valueOf)
                .collect(Collectors.toSet())).build()
        );
    }

    /**
     * Creates a User object from a DynamoDB attribute map.
     *
     * @param item the DynamoDB item map
     * @return a User object
     */
    public static User fromDynamoDbMap(Map<String, AttributeValue> item) {
        return new User(
            item.get("username").s(),
            item.get("email").s(),
            item.get("password").s(),
            item.get("role").s(),
            item.containsKey("attendedEvents") ? item.get("attendedEvents").ns().stream()
                .map(Integer::valueOf)
                .collect(Collectors.toSet()) : Set.of()
        );
    }

    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", role='" + role + '\'' +
                ", attendedEvents=" + attendedEvents +
                '}';
    }
}
