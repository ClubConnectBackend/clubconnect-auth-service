package com.clubconnect.auth.repository;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanResponse;

@Repository
public class UserRepository {

    private final DynamoDbClient dynamoDbClient;
    private final String tableName = "Users";

    public UserRepository(DynamoDbClient dynamoDbClient) {
        this.dynamoDbClient = dynamoDbClient;
    }

    /**
     * Save a user to DynamoDB
     */
    public void saveUser(String username, String email, String password, String role, Set<Integer> attendedEvents) {
        Map<String, AttributeValue> item = new HashMap<>();
        item.put("username", AttributeValue.builder().s(username).build());
        item.put("email", AttributeValue.builder().s(email).build());
        item.put("password", AttributeValue.builder().s(password).build());
        item.put("role", AttributeValue.builder().s(role).build());
    
        // Add attendedEvents only if it's not null or empty
        if (attendedEvents != null && !attendedEvents.isEmpty()) {
            item.put("attendedEvents", AttributeValue.builder().ns(attendedEvents.stream()
                    .map(String::valueOf)
                    .collect(Collectors.toSet())).build());
        }
    
        PutItemRequest request = PutItemRequest.builder()
                .tableName(tableName)
                .item(item)
                .build();
    
        dynamoDbClient.putItem(request);
    }

    /**
     * Find a user by username
     */
    public Map<String, AttributeValue> findUserByUsername(String username) {
        GetItemRequest request = GetItemRequest.builder()
                .tableName(tableName)
                .key(Map.of("username", AttributeValue.builder().s(username).build()))
                .build();

        GetItemResponse response = dynamoDbClient.getItem(request);

        return response.hasItem() ? response.item() : null;
    }

    /**
     * Update attended events for a user
     */
    public void updateAttendedEvents(String username, Set<Integer> attendedEvents) {
        saveUser(
                username,
                findUserByUsername(username).get("email").s(),
                findUserByUsername(username).get("password").s(),
                findUserByUsername(username).get("role").s(),
                attendedEvents
        );
    }

    /**
     * Delete a user by username
     */
    public void deleteUser(String username) {
        Map<String, AttributeValue> key = Map.of("username", AttributeValue.builder().s(username).build());

        DeleteItemRequest request = DeleteItemRequest.builder()
                .tableName(tableName)
                .key(key)
                .build();

        dynamoDbClient.deleteItem(request);
    }

    /**
     * Find a user by email
     *
     * @param email the email to search
     * @return a map of user attributes
     */
    public Map<String, AttributeValue> findUserByEmail(String email) {
        // Create a request to scan items by email attribute
        ScanRequest scanRequest = ScanRequest.builder()
                .tableName(tableName)
                .filterExpression("email = :emailValue")
                .expressionAttributeValues(Map.of(":emailValue", AttributeValue.builder().s(email).build()))
                .build();

        // Execute the request
        ScanResponse scanResponse = dynamoDbClient.scan(scanRequest);

        // Return the first match if it exists
        return scanResponse.count() > 0 ? scanResponse.items().get(0) : null;
    }
}
