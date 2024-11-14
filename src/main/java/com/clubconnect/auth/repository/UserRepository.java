package com.clubconnect.auth.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

@Repository
public class UserRepository {

    private final DynamoDbClient dynamoDbClient;
    private final String tableName = "Users"; 

    public UserRepository(DynamoDbClient dynamoDbClient) {
        this.dynamoDbClient = dynamoDbClient;
    }

    /**
     * Save user with roles to DynamoDB
     *
     * @param username the username of the user
     * @param email    the email of the user
     * @param password the encoded password of the user
     * @param roles    the roles assigned to the user
     */
    public void saveUser(String username, String email, String password, List<String> roles) {
        Map<String, AttributeValue> item = new HashMap<>();
        item.put("username", AttributeValue.builder().s(username).build());
        item.put("email", AttributeValue.builder().s(email).build());
        item.put("password", AttributeValue.builder().s(password).build());
        item.put("roles", AttributeValue.builder().l(
                roles.stream().map(role -> AttributeValue.builder().s(role).build()).toList()
        ).build());

        PutItemRequest request = PutItemRequest.builder()
                .tableName("Users")
                .item(item)
                .build();

        dynamoDbClient.putItem(request);
    }

    /**
     * Find a user by username
     *
     * @param username the username to search
     * @return a map of user attributes
     */
    public Map<String, AttributeValue> findUserByUsername(String username) {
        // Create a request to get an item by key
        GetItemRequest request = GetItemRequest.builder()
                .tableName(tableName)
                .key(Map.of("username", AttributeValue.builder().s(username).build()))
                .build();

        // Execute the request
        GetItemResponse response = dynamoDbClient.getItem(request);

        // Return the item if it exists, otherwise null
        return response.hasItem() ? response.item() : null;
    }
}
