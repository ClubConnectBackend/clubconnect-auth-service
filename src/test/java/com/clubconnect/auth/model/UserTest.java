package com.clubconnect.auth.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

class UserTest {

    @Test
    void testToDynamoDbMap() {
        User user = new User("testUser", "test@example.com", "password123", "ROLE_USER", Set.of(1, 2, 3));

        Map<String, AttributeValue> dynamoDbMap = user.toDynamoDbMap();

        assertEquals("testUser", dynamoDbMap.get("username").s());
        assertEquals("test@example.com", dynamoDbMap.get("email").s());
        assertEquals("password123", dynamoDbMap.get("password").s());
        assertEquals("ROLE_USER", dynamoDbMap.get("role").s());
        assertEquals(Set.of("1", "2", "3"), Set.copyOf(dynamoDbMap.get("attendedEvents").ns()));
    }

    @Test
    void testFromDynamoDbMap() {
        Map<String, AttributeValue> dynamoDbMap = Map.of(
            "username", AttributeValue.builder().s("testUser").build(),
            "email", AttributeValue.builder().s("test@example.com").build(),
            "password", AttributeValue.builder().s("password123").build(),
            "role", AttributeValue.builder().s("ROLE_USER").build(),
            "attendedEvents", AttributeValue.builder().ns("1", "2", "3").build()
        );

        User user = User.fromDynamoDbMap(dynamoDbMap);

        assertEquals("testUser", user.getUsername());
        assertEquals("test@example.com", user.getEmail());
        assertEquals("password123", user.getPassword());
        assertEquals("ROLE_USER", user.getRole());
        assertEquals(Set.of(1, 2, 3), user.getAttendedEvents());
    }

    @Test
    void testFromDynamoDbMapWithEmptyValues() {
        Map<String, AttributeValue> dynamoDbMap = Map.of(
            "username", AttributeValue.builder().s("testUser").build(),
            "email", AttributeValue.builder().s("").build(),
            "password", AttributeValue.builder().s("").build(),
            "role", AttributeValue.builder().s("ROLE_USER").build()
        );

        User user = User.fromDynamoDbMap(dynamoDbMap);

        assertEquals("testUser", user.getUsername());
        assertEquals("", user.getEmail());
        assertEquals("", user.getPassword());
        assertEquals("ROLE_USER", user.getRole());
        assertTrue(user.getAttendedEvents().isEmpty());
    }

    @Test
    void testDefaultConstructor() {
        User user = new User();
        assertNull(user.getUsername());
        assertNull(user.getEmail());
        assertNull(user.getPassword());
        assertNull(user.getRole());
        assertNull(user.getAttendedEvents());
    }

    @Test
    void testParameterizedConstructor() {
        User user = new User("testUser", "test@example.com", "password123", "ROLE_ADMIN", Set.of(100, 101));

        assertEquals("testUser", user.getUsername());
        assertEquals("test@example.com", user.getEmail());
        assertEquals("password123", user.getPassword());
        assertEquals("ROLE_ADMIN", user.getRole());
        assertEquals(Set.of(100, 101), user.getAttendedEvents());
    }

}
