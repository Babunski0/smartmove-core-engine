package com.smartmove.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartmove.domain.entity.Vehicle;
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

/**
 * @author jniyi
 * @project smartmove-core-engine - 2026
 * @created 16.02.2026
 */

/**
 * This repository reads and writes Vehicle data from/to JSON files.
 * No database needed - just JSON files in the filesystem.
 *
 * Features:
 * - Load all vehicles from JSON file
 * - Load vehicle by ID
 * - Save vehicle (single or bulk)
 * - Delete vehicle
 * - Query by city and state
 * - Get vehicle count
 */
@Repository
@RequiredArgsConstructor
@Slf4j
public class FileBasedVehicleRepository {

    @Value("${smartmove.data.vehicles-file:./data/vehicles.json}")
    private String vehiclesFilePath;

    private final ObjectMapper objectMapper;

    /**
     * Load all vehicles from JSON file
     * @return List of all vehicles from the file
     * @throws SmartMoveException if file reading fails
     */
    public List<Vehicle> loadAllVehicles() {
        log.debug("Loading all vehicles from: {}", vehiclesFilePath);

        try {
            Path path = Paths.get(vehiclesFilePath);

            // If file doesn't exist, return empty list
            if (!Files.exists(path)) {
                log.warn("Vehicles file not found: {}", vehiclesFilePath);
                return new ArrayList<>();
            }

            // Read file content
            String jsonContent = Files.readString(path);

            // Parse JSON to Vehicle array
            Vehicle[] vehicles = objectMapper
                    .readerFor(Vehicle[].class)
                    .without(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                    .readValue(jsonContent);

            log.info("Loaded {} vehicles from JSON file", vehicles.length);
            return new ArrayList<>(Arrays.asList(vehicles));

        } catch (IOException e) {
            log.error("Error loading vehicles from JSON file: {}", e.getMessage());
            throw new SmartMoveException("Failed to load vehicles from file", e);
        }
    }

    /**
     * Load a single vehicle by ID
     * @param vehicleId The vehicle ID to load
     * @return The vehicle, or null if not found
     * @throws SmartMoveException if file reading fails
     */
    public Vehicle loadVehicleById(String vehicleId) {
        log.debug("Loading vehicle: {}", vehicleId);

        try {
            return loadAllVehicles().stream()
                    .filter(v -> v.getVehicleId().equals(vehicleId))
                    .findFirst()
                    .orElse(null);
        } catch (Exception e) {
            log.error("Error loading vehicle {}: {}", vehicleId, e.getMessage());
            throw new SmartMoveException("Failed to load vehicle: " + vehicleId, e);
        }
    }

    /**
     * Save a vehicle to the JSON file
     * Loads all vehicles, updates or adds the specified vehicle,
     * and writes back to file.
     * @param vehicle The vehicle to save
     * @throws SmartMoveException if file operations fail
     */
    public void saveVehicle(Vehicle vehicle) {
        log.debug("Saving vehicle: {}", vehicle.getVehicleId());

        try {
        	
        	if (vehicle.getType() == null && vehicle.getClass() != null) {
                if (vehicle instanceof com.smartmove.domain.entity.Bicycle) {
                    vehicle.setType(com.smartmove.domain.enums.VehicleType.BICYCLE);
                } else if (vehicle instanceof com.smartmove.domain.entity.ElectricScooter) {
                    vehicle.setType(com.smartmove.domain.enums.VehicleType.ELECTRIC_SCOOTER);
                } else if (vehicle instanceof com.smartmove.domain.entity.Moped) {
                    vehicle.setType(com.smartmove.domain.enums.VehicleType.MOPED);
                }
            }
            // Load all vehicles
            List<Vehicle> vehicles = loadAllVehicles();

            // Find and update the vehicle, or add if new
            boolean found = false;
            for (int i = 0; i < vehicles.size(); i++) {
                if (vehicles.get(i).getVehicleId().equals(vehicle.getVehicleId())) {
                    vehicles.set(i, vehicle);
                    found = true;
                    break;
                }
            }

            if (!found) {
                vehicles.add(vehicle);
                log.debug("Added new vehicle: {}", vehicle.getVehicleId());
            }
            
            


            // Write back to file
            saveAllVehicles(vehicles);
            log.debug("Vehicle saved successfully: {}", vehicle.getVehicleId());

        } catch (Exception e) {
            log.error("Error saving vehicle: {}", e.getMessage());
            throw new SmartMoveException("Failed to save vehicle to file", e);
        }
    }

    /**
     * Save all vehicles to the JSON file
     * Creates directory if not exists and writes the entire list.
     * @param vehicles List of vehicles to save
     * @throws SmartMoveException if file operations fail
     */
    public void saveAllVehicles(List<Vehicle> vehicles) {
        log.debug("Saving {} vehicles to JSON file", vehicles.size());

        try {
            // Create directory if not exists
            Path path = Paths.get(vehiclesFilePath);
            Files.createDirectories(path.getParent());

            // Convert to JSON and write to file
            String jsonContent = objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(vehicles);

            Files.writeString(path, jsonContent);

            log.info("Saved {} vehicles to JSON file: {}", vehicles.size(), vehiclesFilePath);

        } catch (IOException e) {
            log.error("Error writing vehicles to JSON file: {}", e.getMessage());
            throw new SmartMoveException("Failed to write vehicles to file", e);
        }
    }

    /**
     * Delete a vehicle from the JSON file
     *
     * @param vehicleId The vehicle ID to delete
     * @throws SmartMoveException if file operations fail
     */
    public void deleteVehicle(String vehicleId) {
        log.debug("Deleting vehicle: {}", vehicleId);

        try {
            List<Vehicle> vehicles = loadAllVehicles();

            boolean removed = vehicles.removeIf(v -> v.getVehicleId().equals(vehicleId));

            if (removed) {
                saveAllVehicles(vehicles);
                log.info("Vehicle deleted successfully: {}", vehicleId);
            } else {
                log.warn("Vehicle not found for deletion: {}", vehicleId);
            }

        } catch (Exception e) {
            log.error("Error deleting vehicle: {}", e.getMessage());
            throw new SmartMoveException("Failed to delete vehicle from file", e);
        }
    }

    /**
     * Get count of vehicles
     *
     * @return Number of vehicles in the file
     */
    public int getVehicleCount() {
        return loadAllVehicles().size();
    }

    /**
     * Find vehicles by state
     *
     * @param state The vehicle state to filter by
     * @return List of vehicles matching the state
     */
    public List<Vehicle> findVehiclesByState(String state) {
        log.debug("Finding vehicles with state: {}", state);

        try {
            return loadAllVehicles().stream()
                    .filter(v -> v.getCurrentState().toString().equals(state))
                    .toList();
        } catch (Exception e) {
            log.error("Error finding vehicles by state: {}", e.getMessage());
            throw new SmartMoveException("Failed to find vehicles by state", e);
        }
    }

    /**
     * Find vehicles by city
     *
     * @param city The city to filter by
     * @return List of vehicles in the city
     */
    public List<Vehicle> findVehiclesByCity(String city) {
        log.debug("Finding vehicles in city: {}", city);

        try {
            return loadAllVehicles().stream()
                    .filter(v -> v.getCity().toString().equals(city))
                    .toList();
        } catch (Exception e) {
            log.error("Error finding vehicles by city: {}", e.getMessage());
            throw new SmartMoveException("Failed to find vehicles by city", e);
        }
    }
}
