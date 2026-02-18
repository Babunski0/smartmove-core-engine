package com.smartmove.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartmove.domain.entity.RentalSession;
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
 * This repository reads and writes RentalSession data from/to JSON files.
 * No database needed - just JSON files in the filesystem.
 *
 * Features:
 * - Load all rentals from JSON file
 * - Load rental by ID
 * - Save rental (single or bulk)
 * - Delete rental
 * - Query by user ID and vehicle ID
 * - Find completed and active rentals
 * - Get rental count
 */
@Repository
@RequiredArgsConstructor
@Slf4j
public class FileBasedRentalRepository {

    @Value("${smartmove.data.rentals-file:./data/rentals.json}")
    private String rentalsFilePath;

    private final ObjectMapper objectMapper;

    /**
     * Load all rentals from JSON file
     *
     * @return List of all rental sessions from the file
     * @throws SmartMoveException if file reading fails
     */
    public List<RentalSession> loadAllRentals() {
        log.debug("Loading all rentals from: {}", rentalsFilePath);

        try {
            Path path = Paths.get(rentalsFilePath);

            // If file doesn't exist, return empty list
            if (!Files.exists(path)) {
                log.warn("Rentals file not found: {}", rentalsFilePath);
                return new ArrayList<>();
            }

            // Read file content
            String jsonContent = Files.readString(path);

            // Parse JSON to RentalSession array
            RentalSession[] rentals = objectMapper.readValue(jsonContent, RentalSession[].class);

            log.info("Loaded {} rentals from JSON file", rentals.length);
            return new ArrayList<>(Arrays.asList(rentals));

        } catch (IOException e) {
            log.error("Error loading rentals from JSON file: {}", e.getMessage());
            throw new SmartMoveException("Failed to load rentals from file", e);
        }
    }

    /**
     * Load a single rental by ID
     *
     * @param rentalId The rental ID to load
     * @return The rental session, or null if not found
     * @throws SmartMoveException if file reading fails
     */
    public RentalSession loadRentalById(String rentalId) {
        log.debug("Loading rental: {}", rentalId);

        try {
            return loadAllRentals().stream()
                    .filter(r -> r.getRentalId().equals(rentalId))
                    .findFirst()
                    .orElse(null);
        } catch (Exception e) {
            log.error("Error loading rental {}: {}", rentalId, e.getMessage());
            throw new SmartMoveException("Failed to load rental: " + rentalId, e);
        }
    }

    /**
     * Save a rental to the JSON file
     *
     * Loads all rentals, updates or adds the specified rental,
     * and writes back to file.
     *
     * @param rental The rental to save
     * @throws SmartMoveException if file operations fail
     */
    public void saveRental(RentalSession rental) {
        log.debug("Saving rental: {}", rental.getRentalId());

        try {
            // Load all rentals
            List<RentalSession> rentals = loadAllRentals();

            // Find and update the rental, or add if new
            boolean found = false;
            for (int i = 0; i < rentals.size(); i++) {
                if (rentals.get(i).getRentalId().equals(rental.getRentalId())) {
                    rentals.set(i, rental);
                    found = true;
                    break;
                }
            }

            if (!found) {
                rentals.add(rental);
                log.debug("Added new rental: {}", rental.getRentalId());
            }

            // Write back to file
            saveAllRentals(rentals);
            log.debug("Rental saved successfully: {}", rental.getRentalId());

        } catch (Exception e) {
            log.error("Error saving rental: {}", e.getMessage());
            throw new SmartMoveException("Failed to save rental to file", e);
        }
    }

    /**
     * Save all rentals to the JSON file
     *
     * Creates directory if not exists and writes the entire list.
     *
     * @param rentals List of rentals to save
     * @throws SmartMoveException if file operations fail
     */
    public void saveAllRentals(List<RentalSession> rentals) {
        log.debug("Saving {} rentals to JSON file", rentals.size());

        try {
            // Create directory if not exists
            Path path = Paths.get(rentalsFilePath);
            Files.createDirectories(path.getParent());

            // Convert to JSON and write to file
            String jsonContent = objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(rentals);

            Files.writeString(path, jsonContent);

            log.info("Saved {} rentals to JSON file: {}", rentals.size(), rentalsFilePath);

        } catch (IOException e) {
            log.error("Error writing rentals to JSON file: {}", e.getMessage());
            throw new SmartMoveException("Failed to write rentals to file", e);
        }
    }

    /**
     * Delete a rental from the JSON file
     *
     * @param rentalId The rental ID to delete
     * @throws SmartMoveException if file operations fail
     */
    public void deleteRental(String rentalId) {
        log.debug("Deleting rental: {}", rentalId);

        try {
            List<RentalSession> rentals = loadAllRentals();

            boolean removed = rentals.removeIf(r -> r.getRentalId().equals(rentalId));

            if (removed) {
                saveAllRentals(rentals);
                log.info("Rental deleted successfully: {}", rentalId);
            } else {
                log.warn("Rental not found for deletion: {}", rentalId);
            }

        } catch (Exception e) {
            log.error("Error deleting rental: {}", e.getMessage());
            throw new SmartMoveException("Failed to delete rental from file", e);
        }
    }

    /**
     * Get count of rentals
     *
     * @return Number of rentals in the file
     */
    public int getRentalCount() {
        return loadAllRentals().size();
    }

    /**
     * Find rentals by user ID
     *
     * @param userId The user ID to filter by
     * @return List of rentals for the user
     */
    public List<RentalSession> findRentalsByUserId(String userId) {
        log.debug("Finding rentals for user: {}", userId);

        try {
            return loadAllRentals().stream()
                    .filter(r -> r.getUserId().equals(userId))
                    .toList();
        } catch (Exception e) {
            log.error("Error finding rentals by user: {}", e.getMessage());
            throw new SmartMoveException("Failed to find rentals by user", e);
        }
    }

    /**
     * Find rentals by vehicle ID
     *
     * @param vehicleId The vehicle ID to filter by
     * @return List of rentals for the vehicle
     */
    public List<RentalSession> findRentalsByVehicleId(String vehicleId) {
        log.debug("Finding rentals for vehicle: {}", vehicleId);

        try {
            return loadAllRentals().stream()
                    .filter(r -> r.getVehicleId().equals(vehicleId))
                    .toList();
        } catch (Exception e) {
            log.error("Error finding rentals by vehicle: {}", e.getMessage());
            throw new SmartMoveException("Failed to find rentals by vehicle", e);
        }
    }

    /**
     * Find completed rentals
     *
     * @return List of completed rental sessions
     */
    public List<RentalSession> findCompletedRentals() {
        log.debug("Finding completed rentals");

        try {
            return loadAllRentals().stream()
                    .filter(RentalSession::isCompleted)
                    .toList();
        } catch (Exception e) {
            log.error("Error finding completed rentals: {}", e.getMessage());
            throw new SmartMoveException("Failed to find completed rentals", e);
        }
    }

    /**
     * Find active rentals (not completed)
     *
     * @return List of active rental sessions
     */
    public List<RentalSession> findActiveRentals() {
        log.debug("Finding active rentals");

        try {
            return loadAllRentals().stream()
                    .filter(r -> !r.isCompleted())
                    .toList();
        } catch (Exception e) {
            log.error("Error finding active rentals: {}", e.getMessage());
            throw new SmartMoveException("Failed to find active rentals", e);
        }
    }
}
