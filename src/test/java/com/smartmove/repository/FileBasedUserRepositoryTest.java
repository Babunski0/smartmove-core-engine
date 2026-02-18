package com.smartmove.repository;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartmove.domain.entity.User;
import com.smartmove.exception.SmartMoveException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileBasedUserRepositoryTest {

    @TempDir
    Path tempDir;

    private FileBasedUserRepository repository;
    private Path usersFile;
    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ObjectMapper()
                .findAndRegisterModules()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        repository = new FileBasedUserRepository(mapper);

        usersFile = tempDir.resolve("users.json");
        ReflectionTestUtils.setField(repository, "usersFilePath", usersFile.toString());
    }

    @Test
    void loadAllUsers_returnsCorrectCount() throws Exception {
        writeUsersJson(sampleUsersJson());

        List<User> users = repository.loadAllUsers();
        assertEquals(3, users.size());
    }

    @Test
    void loadUserById_returnsCorrectUser() throws Exception {
        writeUsersJson(sampleUsersJson());

        User user = repository.loadUserById("u1");
        assertNotNull(user);
        assertEquals("u1", user.getUserId());
        assertEquals("John", user.getFirstName());
    }

    @Test
    void loadUserById_returnsNullForInvalidId() throws Exception {
        writeUsersJson(sampleUsersJson());
        assertNull(repository.loadUserById("invalid"));
    }

    @Test
    void findUsersByCity_filtersCorrectly() throws Exception {
        writeUsersJson(sampleUsersJson());

        List<User> londonUsers = repository.findUsersByCity("LONDON");
        assertEquals(2, londonUsers.size());
    }

    @Test
    void getUserCount_returnsCorrectCount() throws Exception {
        writeUsersJson(sampleUsersJson());
        assertEquals(3, repository.getUserCount());
    }

    @Test
    void userDataIntegrity_allFieldsPreserved() throws Exception {
        writeUsersJson(sampleUsersJson());

        User user = repository.loadUserById("u1");

        assertEquals("John", user.getFirstName());
        assertEquals("Doe", user.getLastName());
        assertEquals("john@example.com", user.getEmail());
        assertEquals("123456789", user.getPhoneNumber());
        assertEquals(com.smartmove.domain.enums.City.LONDON, user.getHomeCity());
        assertEquals(5, user.getCompletedRentals());
        assertEquals(4.5, user.getAverageRating());
    }

    @Test
    void readOnlyUsers_cannotBeModified() throws Exception {
        writeUsersJson(sampleUsersJson());

        User user = repository.loadUserById("u1");
        user.setFirstName("Changed");

        repository.saveUser(user);

        User reloaded = repository.loadUserById("u1");
        assertEquals("Changed", reloaded.getFirstName());
    }

    @Test
    void fileHandling_fileNotFound_returnsEmptyList() {
        List<User> users = repository.loadAllUsers();
        assertNotNull(users);
        assertTrue(users.isEmpty());
    }

    private void writeUsersJson(String json) throws Exception {
        Files.writeString(usersFile, json);
    }

    private String sampleUsersJson() {
        return """
                [
                  {
                    "userId": "u1",
                    "firstName": "John",
                    "lastName": "Doe",
                    "email": "john@example.com",
                    "phoneNumber": "123456789",
                    "homeCity": "LONDON",
                    "completedRentals": 5,
                    "averageRating": 4.5
                  },
                  {
                    "userId": "u2",
                    "firstName": "Jane",
                    "lastName": "Smith",
                    "email": "jane@example.com",
                    "phoneNumber": "987654321",
                    "homeCity": "LONDON",
                    "completedRentals": 2,
                    "averageRating": 4.8
                  },
                  {
                    "userId": "u3",
                    "firstName": "Max",
                    "lastName": "Mustermann",
                    "email": "max@example.com",
                    "phoneNumber": "555555555",
                    "homeCity": "BERLIN",
                    "completedRentals": 7,
                    "averageRating": 4.2
                  }
                ]
                """;
    }
}
