package com.clubconnect.auth.service;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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
     * Save or update a user with the provided roles and attended events
     *
     * @param username       the username of the user
     * @param email          the email of the user
     * @param password       the password of the user
     * @param role           the role to assign to the user (e.g., ROLE_USER, ROLE_ADMIN)
     * @param attendedEvents the events the user is attending
     */
    public void saveUser(String username, String email, String password, String role, Set<Integer> attendedEvents) {
        String encodedPassword = passwordEncoder.encode(password);

        // Save the user with the repository
        userRepository.saveUser(username, email, encodedPassword, role, attendedEvents);
    }

    /**
     * Retrieve a user by username
     *
     * @param username the username to search
     * @return Optional containing user data if found, otherwise empty
     */
    public Optional<Map<String, AttributeValue>> findByUsername(String username) {
        Map<String, AttributeValue> user = userRepository.findUserByUsername(username);
        return Optional.ofNullable(user).filter(u -> !u.isEmpty());
    }

    /**
     * Retrieve roles for a given user
     *
     * @param username the username to fetch roles for
     * @return Optional containing the role, or empty if not found
     */
    public Optional<String> getUserRole(String username) {
        return findByUsername(username).map(user -> {
            AttributeValue roleAttr = user.get("role");
            return roleAttr != null ? roleAttr.s() : null;
        });
    }

    /**
     * Retrieve attended events for a given user
     *
     * @param username the username to fetch events for
     * @return Optional containing a list of event IDs, or empty if not found
     */
    public Optional<Set<Integer>> getAttendedEvents(String username) {
        return findByUsername(username).map(user -> {
            AttributeValue eventsAttr = user.get("attendedEvents");
            if (eventsAttr != null && eventsAttr.hasNs()) {
                return eventsAttr.ns().stream()
                        .map(Integer::valueOf) // Convert from String to Integer
                        .collect(Collectors.toSet());
            }
            return Set.of(); // Return an empty set if no events are found
        });
    }

    /**
     * Add a new event to a user's attended events list
     *
     * @param username the username of the user
     * @param eventId  the event ID to add
     */
    public void addEventToUser(String username, Integer eventId) {
        var userOptional = findByUsername(username);
        if (userOptional.isPresent()) {
            Map<String, AttributeValue> userAttributes = userOptional.get();
            Set<Integer> attendedEvents = userAttributes.containsKey("attendedEvents")
                    ? userAttributes.get("attendedEvents").ns().stream()
                        .map(Integer::valueOf)
                        .collect(Collectors.toSet())
                    : new HashSet<>();
            attendedEvents.add(eventId);
            updateAttendedEvents(username, attendedEvents);
        } else {
            throw new IllegalArgumentException("User does not exist: " + username);
        }
    }


    /**
     * Remove an event from a user's attended events list
     *
     * @param username the username of the user
     * @param eventId  the event ID to remove
     */
    public void removeEventFromUser(String username, int eventId) {
        Optional<Set<Integer>> attendedEventsOpt = getAttendedEvents(username);
        if (attendedEventsOpt.isPresent()) {
            Set<Integer> attendedEvents = attendedEventsOpt.get();
            attendedEvents.remove(eventId);
            updateAttendedEvents(username, attendedEvents);
        }
    }

    /**
     * Update attended events for a user
     *
     * @param username       the username of the user
     * @param attendedEvents the list of attended event IDs
     */
    public void updateAttendedEvents(String username, Set<Integer> attendedEvents) {
        userRepository.updateAttendedEvents(username, attendedEvents);
    }

    /**
     * Delete a user by username
     *
     * @param username the username to delete
     */
    public void deleteUser(String username) {
        userRepository.deleteUser(username);
    }

    /**
     * Check if a user exists by username
     *
     * @param username the username to check
     * @return true if the user exists, false otherwise
     */
    public boolean userExists(String username) {
        return findByUsername(username).isPresent();
    }
    
    public boolean emailExists(String email) {
        return userRepository.findUserByEmail(email) != null;
    }
}
