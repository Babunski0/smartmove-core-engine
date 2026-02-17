package com.smartmove.service;

/**
 * @author jniyi
 * @project smartmove-core-engine - 2026
 * @created 17.02.2026
 */

import com.smartmove.domain.entity.GPSLocation;
import com.smartmove.domain.entity.RentalSession;
import com.smartmove.domain.entity.User;
import com.smartmove.domain.entity.Vehicle;
import com.smartmove.domain.enums.VehicleState;
import com.smartmove.dto.request.EndRentalRequest;
import com.smartmove.dto.request.ReserveVehicleRequest;
import com.smartmove.dto.request.StartRentalRequest;
import com.smartmove.dto.response.RentalResponse;
import com.smartmove.exception.SmartMoveException;
import com.smartmove.exception.VehicleNotAvailableException;
import com.smartmove.mapper.RentalMapper;
import com.smartmove.repository.FileBasedRentalRepository;
import com.smartmove.repository.FileBasedUserRepository;
import com.smartmove.repository.FileBasedVehicleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * SmartMoveRentalService - Core rental management business logic
 *
 * Handles all rental operations:
 * - Reserve vehicles
 * - Start rentals
 * - End rentals with cost calculation
 * - Manage rental lifecycle and state transitions
 * - Load and validate user data during rental operations
 *
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RentalService {

    private final FileBasedVehicleRepository vehicleRepository;
    private final FileBasedRentalRepository rentalRepository;
    private final FileBasedUserRepository userRepository;
    private final VehicleLockManager lockManager;
    private final StateTransitionValidator stateValidator;
    private final RegulatoryEngine regulatoryEngine;
    private final RentalMapper rentalMapper;

    /**
     * Reserve a vehicle for a user
     *
     * Validates user exists, checks vehicle availability, and creates rental session.
     *
     * @param request The reservation request with vehicleId, userId, city
     * @return The created rental response
     */
    public RentalResponse reserveVehicle(ReserveVehicleRequest request) {
        log.info("Reserving vehicle {} for user {}", request.getVehicleId(), request.getUserId());

        lockManager.lock(request.getVehicleId());

        try {
            // Load and validate user
            User user = userRepository.loadUserById(request.getUserId());
            if (user == null) {
                throw new SmartMoveException("User not found: " + request.getUserId());
            }
            log.debug("User loaded: {} {}", user.getFirstName(), user.getLastName());

            // Load vehicle
            Vehicle vehicle = vehicleRepository.loadVehicleById(request.getVehicleId());
            if (vehicle == null) {
                throw new VehicleNotAvailableException("Vehicle not found: " + request.getVehicleId());
            }

            // Validate vehicle state can transition to RESERVED
            if (!stateValidator.isValidTransition(vehicle.getCurrentState(), VehicleState.RESERVED)) {
                throw new VehicleNotAvailableException(
                        "Cannot reserve vehicle in state: " + vehicle.getCurrentState()
                );
            }

            // Update vehicle state
            vehicle.setState(VehicleState.RESERVED);
            String rentalId = UUID.randomUUID().toString();
            vehicle.setActiveRentalId(rentalId);
            vehicleRepository.saveVehicle(vehicle);

            // Create rental session
            RentalSession rental = new RentalSession(
                    rentalId,
                    vehicle.getVehicleId(),
                    user,
                    request.getCity(),
                    vehicle.getCurrentLocation()
            );

            // Save rental
            rentalRepository.saveRental(rental);

            log.info("Vehicle reserved successfully. Rental ID: {}", rental.getRentalId());

            return rentalMapper.toResponse(rental);

        } catch (VehicleNotAvailableException e) {
            log.warn("Vehicle not available for reservation: {}", e.getMessage());
            throw e;
        } catch (SmartMoveException e) {
            log.error("SmartMove exception during reservation: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error reserving vehicle: {}", e.getMessage());
            throw new SmartMoveException("Failed to reserve vehicle", e);
        } finally {
            lockManager.unlock(request.getVehicleId());
        }
    }

    /**
     * Start a rental for a reserved vehicle
     *
     * Validates rental exists, updates vehicle to IN_USE state, records start time and location.
     *
     * @param rentalId The rental ID
     * @param request The start rental request with vehicleId
     * @return The updated rental response
     */
    public RentalResponse startRental(String rentalId, StartRentalRequest request) {
        log.info("Starting rental {} for vehicle {}", rentalId, request.getVehicleId());

        lockManager.lock(request.getVehicleId());

        try {
            // Load rental
            RentalSession rental = rentalRepository.loadRentalById(rentalId);
            if (rental == null) {
                throw new SmartMoveException("Rental not found: " + rentalId);
            }

            // Validate user exists
            User user = userRepository.loadUserById(rental.getUser().getUserId());
            if (user == null) {
                throw new SmartMoveException("User not found for rental: " + rentalId);
            }
            log.debug("User validated for rental: {}", user.getUserId());

            // Load vehicle
            Vehicle vehicle = vehicleRepository.loadVehicleById(request.getVehicleId());
            if (vehicle == null) {
                throw new VehicleNotAvailableException("Vehicle not found: " + request.getVehicleId());
            }

            // Validate vehicle state can transition to IN_USE
            if (!stateValidator.isValidTransition(vehicle.getCurrentState(), VehicleState.IN_USE)) {
                throw new VehicleNotAvailableException(
                        "Cannot start rental with vehicle in state: " + vehicle.getCurrentState()
                );
            }

            // Update vehicle state
            vehicle.setState(VehicleState.IN_USE);
            vehicle.setLocked(false);
            vehicleRepository.saveVehicle(vehicle);

            // Update rental with start information
            rental.setStartTime(Instant.now());
            rental.setStartLocation(vehicle.getCurrentLocation());
            rentalRepository.saveRental(rental);

            log.info("Rental started successfully. Rental ID: {}", rentalId);

            return rentalMapper.toResponse(rental);

        } catch (VehicleNotAvailableException e) {
            log.warn("Cannot start rental: {}", e.getMessage());
            throw e;
        } catch (SmartMoveException e) {
            log.error("SmartMove exception during rental start: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error starting rental: {}", e.getMessage());
            throw new SmartMoveException("Failed to start rental", e);
        } finally {
            lockManager.unlock(request.getVehicleId());
        }
    }

    /**
     * End a rental and calculate final cost
     *
     * Records end time and location, calculates distance and cost, updates vehicle state.
     *
     * @param rentalId The rental ID
     * @param vehicleId The vehicle ID
     * @param request The end rental request with end location (latitude, longitude)
     * @return The completed rental response
     */
    public RentalResponse endRental(String rentalId, String vehicleId, EndRentalRequest request) {
        log.info("Ending rental {} for vehicle {}", rentalId, vehicleId);

        lockManager.lock(vehicleId);

        try {
            // Load rental
            RentalSession rental = rentalRepository.loadRentalById(rentalId);
            if (rental == null) {
                throw new SmartMoveException("Rental not found: " + rentalId);
            }

            // Load vehicle
            Vehicle vehicle = vehicleRepository.loadVehicleById(vehicleId);
            if (vehicle == null) {
                throw new VehicleNotAvailableException("Vehicle not found: " + vehicleId);
            }

            // Load and validate user
            User user = userRepository.loadUserById(rental.getUser().getUserId());
            if (user == null) {
                throw new SmartMoveException("User not found for rental: " + rentalId);
            }
            log.debug("User loaded for rental end: {}", user.getUserId());

            // Validate rental has start information
            if (rental.getStartTime() == null || rental.getStartLocation() == null) {
                throw new SmartMoveException("Rental has not been started: " + rentalId);
            }

            // Create end location from request
            GPSLocation endLocation = new GPSLocation(
                    request.getLatitude(),
                    request.getLongitude()
            );

            // Calculate distance
            double distanceKm = rental.getStartLocation().distanceTo(endLocation);
            log.debug("Rental distance: {} km", distanceKm);

            // Calculate duration in minutes
            long durationMinutes = rental.getDurationMinutes();
            log.debug("Rental duration: {} minutes", durationMinutes);

            // Calculate cost using regulatory engine (returns double)
            double rentalCost = regulatoryEngine.calculateFinalCost(
                    vehicle,
                    rental.getCity(),
                    distanceKm,
                    durationMinutes
            );
            log.info("Rental cost calculated: {} {}", rentalCost, regulatoryEngine.getCurrencyForCity(rental.getCity()));

            // Update rental with end information
            rental.endRental(endLocation);
            // Convert double to BigDecimal with 2 decimal places
            rental.setTotalCostAmount(BigDecimal.valueOf(rentalCost).setScale(2, java.math.RoundingMode.HALF_UP));
            rental.setCostCurrency(regulatoryEngine.getCurrencyForCity(rental.getCity()));
            rentalRepository.saveRental(rental);

            // Update vehicle state
            vehicle.setState(VehicleState.AVAILABLE);
            vehicle.setLocked(true);
            vehicle.setLocation(endLocation);
            vehicle.setActiveRentalId(null);
            vehicleRepository.saveVehicle(vehicle);

            log.info("Rental ended successfully. Cost: {} {}", rentalCost,
                    regulatoryEngine.getCurrencyForCity(rental.getCity()));

            return rentalMapper.toResponse(rental);

        } catch (SmartMoveException e) {
            log.error("SmartMove exception during rental end: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error ending rental: {}", e.getMessage());
            throw new SmartMoveException("Failed to end rental", e);
        } finally {
            lockManager.unlock(vehicleId);
        }
    }
}
