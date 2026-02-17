package com.smartmove.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartmove.domain.entity.User;
import com.smartmove.exception.SmartMoveException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Repository
@RequiredArgsConstructor
@Slf4j
public class FileBasedUserRepository {

    @Value("${smartmove.data.users-file:./data/users.json}")
    private String usersFilePath;

    private final ObjectMapper objectMapper;

    public List<User> loadAllUsers() {
        log.debug("Loading all users from: {}", usersFilePath);

        try {
            Path path = Paths.get(usersFilePath);

            if (!Files.exists(path)) {
                log.warn("Users file not found: {}", usersFilePath);
                return new ArrayList<>();
            }

            String jsonContent = Files.readString(path);
            User[] users = objectMapper.readValue(jsonContent, User[].class);

            log.info("Loaded {} users from JSON file", users.length);
            return Arrays.asList(users);

        } catch (IOException e) {
            log.error("Error loading users from JSON file: {}", e.getMessage());
            throw new SmartMoveException("Failed to load users from file", e);
        }
    }

    public User loadUserById(String userId) {
        log.debug("Loading user: {}", userId);

        try {
            return loadAllUsers().stream()
                    .filter(u -> u.getUserId().equals(userId)) 
                    .findFirst()
                    .orElse(null);
        } catch (Exception e) {
            log.error("Error loading user {}: {}", userId, e.getMessage());
            throw new SmartMoveException("Failed to load user: " + userId, e);
        }
    }

    public void saveUser(User user) {
        log.debug("Saving user: {}", user.getUserId()); 

        try {
            List<User> users = loadAllUsers();

            boolean found = false;
            for (int i = 0; i < users.size(); i++) {
                if (users.get(i).getUserId().equals(user.getUserId())) { 
                    users.set(i, user);
                    found = true;
                    break;
                }
            }

            if (!found) {
                users.add(user);
                log.debug("Added new user: {}", user.getUserId());
            }

            saveAllUsers(users);

        } catch (Exception e) {
            log.error("Error saving user: {}", e.getMessage());
            throw new SmartMoveException("Failed to save user to file", e);
        }
    }

    public void saveAllUsers(List<User> users) {
        log.debug("Saving {} users to JSON file", users.size());

        try {
            Path path = Paths.get(usersFilePath);
            Files.createDirectories(path.getParent());

            String jsonContent = objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(users);

            Files.writeString(path, jsonContent);

            log.info("Saved {} users to JSON file: {}", users.size(), usersFilePath);

        } catch (IOException e) {
            log.error("Error writing users to JSON file: {}", e.getMessage());
            throw new SmartMoveException("Failed to write users to file", e);
        }
    }

    public void deleteUser(String userId) {
        log.debug("Deleting user: {}", userId);

        try {
            List<User> users = loadAllUsers();

            boolean removed = users.removeIf(u -> u.getUserId().equals(userId)); 

            if (removed) {
                saveAllUsers(users);
                log.info("User deleted successfully: {}", userId);
            } else {
                log.warn("User not found for deletion: {}", userId);
            }

        } catch (Exception e) {
            log.error("Error deleting user: {}", e.getMessage());
            throw new SmartMoveException("Failed to delete user from file", e);
        }
    }

    public int getUserCount() {
        return loadAllUsers().size();
    }
}
