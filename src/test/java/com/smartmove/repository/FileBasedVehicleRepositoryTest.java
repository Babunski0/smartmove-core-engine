package com.smartmove.repository;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartmove.domain.entity.Bicycle;
import com.smartmove.domain.entity.ElectricScooter;
import com.smartmove.domain.entity.Moped;
import com.smartmove.domain.entity.Vehicle;
import com.smartmove.exception.SmartMoveException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileBasedVehicleRepositoryTest {

    @TempDir
    Path tempDir;

    private FileBasedVehicleRepository repository;
    private Path vehiclesFile;
    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ObjectMapper()
                .findAndRegisterModules()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        repository = new FileBasedVehicleRepository(mapper);

        vehiclesFile = tempDir.resolve("vehicles.json");
        ReflectionTestUtils.setField(repository, "vehiclesFilePath", vehiclesFile.toString());
    }

    @Test
    void loadAllVehicles_returnsCorrectCount() throws Exception {
        writeVehiclesJson(sampleVehiclesJson());
        List<Vehicle> all = repository.loadAllVehicles();
        assertEquals(3, all.size());
    }

    @Test
    void loadVehicleById_returnsCorrectVehicle() throws Exception {
        writeVehiclesJson(sampleVehiclesJson());

        Vehicle v = repository.loadVehicleById("bike-001");
        assertNotNull(v);
        assertEquals("bike-001", v.getVehicleId());
        assertTrue(v instanceof Bicycle);
    }

    @Test
    void loadVehicleById_returnsNullForInvalidId() throws Exception {
        writeVehiclesJson(sampleVehiclesJson());
        assertNull(repository.loadVehicleById("does-not-exist"));
    }

    @Test
    void findVehiclesByCity_filtersCorrectly() throws Exception {
        writeVehiclesJson(sampleVehiclesJson());

        List<Vehicle> london = repository.findVehiclesByCity("LONDON");
        assertEquals(2, london.size());
        assertTrue(london.stream().allMatch(v -> v.getCity().toString().equals("LONDON")));
    }

    @Test
    void findVehiclesByCity_returnsEmptyForInvalidCity() throws Exception {
        writeVehiclesJson(sampleVehiclesJson());
        assertTrue(repository.findVehiclesByCity("PARIS").isEmpty());
    }

    @Test
    void findVehiclesByState_filtersCorrectly() throws Exception {
        writeVehiclesJson(sampleVehiclesJson());

        List<Vehicle> available = repository.findVehiclesByState("AVAILABLE");
        assertEquals(2, available.size());
        assertTrue(available.stream().allMatch(v -> v.getCurrentState().toString().equals("AVAILABLE")));
    }

    @Test
    void findVehiclesByState_returnsEmptyForInvalidState() throws Exception {
        writeVehiclesJson(sampleVehiclesJson());
        assertTrue(repository.findVehiclesByState("BROKEN").isEmpty());
    }

    @Test
    void saveVehicle_persistsChanges() throws Exception {
        writeVehiclesJson(sampleVehiclesJson());

        Vehicle bike = repository.loadVehicleById("bike-001");
        assertNotNull(bike);

        bike.setLocked(false);
        bike.setBatteryPercentage(42.0);

        repository.saveVehicle(bike);

        JsonNode root = readVehiclesTree();
        JsonNode bikeNode = findById(root, "bike-001");
        assertNotNull(bikeNode);

        assertFalse(bikeNode.get("locked").asBoolean());
        assertEquals(42.0, bikeNode.get("batteryPercentage").asDouble(), 0.0001);
    }

    @Test
    void saveVehicle_updatesExistingVehicle() throws Exception {
        writeVehiclesJson(sampleVehiclesJson());

        Vehicle scooter = repository.loadVehicleById("scooter-001");
        assertNotNull(scooter);

        scooter.setTemperatureCelsius(99.9);
        repository.saveVehicle(scooter);

        JsonNode root = readVehiclesTree();
        assertEquals(3, root.size(), "Should still have 3 vehicles after update");

        JsonNode scooterNode = findById(root, "scooter-001");
        assertNotNull(scooterNode);
        assertEquals(99.9, scooterNode.get("temperatureCelsius").asDouble(), 0.0001);
    }

    @Test
    void deleteVehicle_removesVehicle() throws Exception {
        writeVehiclesJson(sampleVehiclesJson());

        assertNotNull(repository.loadVehicleById("moped-001"));

        repository.deleteVehicle("moped-001");

        JsonNode root = readVehiclesTree();
        assertEquals(2, root.size(), "Should have 2 vehicles after delete");
        assertNull(findById(root, "moped-001"));
    }

    @Test
    void getVehicleCount_returnsCorrectCount() throws Exception {
        writeVehiclesJson(sampleVehiclesJson());
        assertEquals(3, repository.getVehicleCount());
    }

    @Test
    void polymorphicDeserialization_bicycle_electricScooter_moped() throws Exception {
        writeVehiclesJson(sampleVehiclesJson());

        List<Vehicle> all = repository.loadAllVehicles();
        assertEquals(3, all.size());

        assertTrue(all.stream().anyMatch(v -> v instanceof Bicycle));
        assertTrue(all.stream().anyMatch(v -> v instanceof ElectricScooter));
        assertTrue(all.stream().anyMatch(v -> v instanceof Moped));
    }

    @Test
    void fileHandling_fileNotFound_returnsEmptyList() {
        Path missing = tempDir.resolve("missing-vehicles.json");
        ReflectionTestUtils.setField(repository, "vehiclesFilePath", missing.toString());

        List<Vehicle> all = repository.loadAllVehicles();
        assertNotNull(all);
        assertTrue(all.isEmpty());
    }

    @Test
    void fileHandling_corruptJson_throwsSmartMoveException() throws Exception {
        Files.writeString(vehiclesFile, "THIS IS NOT JSON");
        assertThrows(SmartMoveException.class, () -> repository.loadAllVehicles());
    }

    private void writeVehiclesJson(String jsonArray) throws Exception {
        Files.writeString(vehiclesFile, jsonArray);
    }

    private JsonNode readVehiclesTree() throws Exception {
        String json = Files.readString(vehiclesFile);
        JsonNode root = mapper.readTree(json);
        assertTrue(root.isArray(), "vehicles.json must be a JSON array");
        return root;
    }

    private JsonNode findById(JsonNode array, String vehicleId) {
        for (JsonNode node : array) {
            JsonNode id = node.get("vehicleId");
            if (id != null && vehicleId.equals(id.asText())) {
                return node;
            }
        }
        return null;
    }

    private String sampleVehiclesJson() {
        return """
                [
                  {
                    "vehicleId": "bike-001",
                    "type": "BICYCLE",
                    "city": "LONDON",
                    "currentState": "AVAILABLE",
                    "currentLocation": { "latitude": 51.5074, "longitude": -0.1278, "timestamp": 1707853200000 },
                    "batteryPercentage": 100,
                    "temperatureCelsius": 15.5,
                    "activeRentalId": null,
                    "lastTelemetryUpdate": 1707853200000,
                    "locked": true,
                    "wheelSize": 26,
                    "hasGear": true
                  },
                  {
                    "vehicleId": "scooter-001",
                    "type": "ELECTRIC_SCOOTER",
                    "city": "LONDON",
                    "currentState": "IN_USE",
                    "currentLocation": { "latitude": 51.5090, "longitude": -0.1200, "timestamp": 1707853200000 },
                    "batteryPercentage": 88.5,
                    "temperatureCelsius": 18.0,
                    "activeRentalId": "r-123",
                    "lastTelemetryUpdate": 1707853200000,
                    "locked": false,
                    "motorPowerWatts": 350,
                    "maxSpeedKmh": 25.0
                  },
                  {
                    "vehicleId": "moped-001",
                    "type": "MOPED",
                    "city": "BERLIN",
                    "currentState": "AVAILABLE",
                    "currentLocation": { "latitude": 52.5200, "longitude": 13.4050, "timestamp": 1707853200000 },
                    "batteryPercentage": 77.0,
                    "temperatureCelsius": 12.0,
                    "activeRentalId": null,
                    "lastTelemetryUpdate": 1707853200000,
                    "locked": true,
                    "engineCcm": 125,
                    "hasHelmetSensor": true
                  }
                ]
                """;
    }
}
