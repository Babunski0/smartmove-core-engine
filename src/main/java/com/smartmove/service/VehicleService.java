package com.smartmove.service;

import com.smartmove.domain.entity.Vehicle;
import com.smartmove.dto.request.CreateVehicleRequest;
import com.smartmove.dto.request.UpdateVehicleRequest;
import com.smartmove.dto.response.VehicleListResponse;
import com.smartmove.dto.response.VehicleResponse;
import com.smartmove.exception.SmartMoveException;
import com.smartmove.mapper.VehicleMapper;
import com.smartmove.repository.FileBasedVehicleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author jniyi
 * @project smartmove-core-engine - 2026
 * @created 16.02.2026
 */

@Service
@RequiredArgsConstructor
@Slf4j
public class VehicleService {

    private final FileBasedVehicleRepository vehicleRepository;
    private final VehicleLockManager lockManager;
    private final VehicleMapper vehicleMapper;

    /**
     * Get all vehicles
     */
    public List<VehicleListResponse> getAllVehicles() {
        log.debug("Getting all vehicles");

        try {
            return vehicleRepository.loadAllVehicles().stream()
                    .map(vehicleMapper::toListResponse)
                    .toList();
        } catch (Exception e) {
            log.error("Error getting all vehicles: {}", e.getMessage());
            throw new SmartMoveException("Failed to retrieve vehicles", e);
        }
    }

    /**
     * Get vehicles by city
     */
    public List<VehicleListResponse> getVehiclesByCity(String city) {
        log.debug("Getting vehicles by city: {}", city);

        try {
            return vehicleRepository.findVehiclesByCity(city).stream()
                    .map(vehicleMapper::toListResponse)
                    .toList();
        } catch (Exception e) {
            log.error("Error getting vehicles by city {}: {}", city, e.getMessage());
            throw new SmartMoveException("Failed to retrieve vehicles by city", e);
        }
    }

    /**
     * Get vehicles by state
     */
    public List<VehicleListResponse> getVehiclesByState(String state) {
        log.debug("Getting vehicles by state: {}", state);

        try {
            return vehicleRepository.findVehiclesByState(state).stream()
                    .map(vehicleMapper::toListResponse)
                    .toList();
        } catch (Exception e) {
            log.error("Error getting vehicles by state {}: {}", state, e.getMessage());
            throw new SmartMoveException("Failed to retrieve vehicles by state", e);
        }
    }

    /**
     * Get detailed vehicle status/information
     */
    public VehicleResponse getVehicleStatus(String vehicleId) {
        log.debug("Getting vehicle status: {}", vehicleId);

        try {
            Vehicle vehicle = vehicleRepository.loadVehicleById(vehicleId);

            if (vehicle == null) {
                log.warn("Vehicle not found: {}", vehicleId);
                throw new SmartMoveException("Vehicle not found: " + vehicleId);
            }

            return vehicleMapper.toResponse(vehicle);
        } catch (Exception e) {
            log.error("Error getting vehicle status {}: {}", vehicleId, e.getMessage());
            throw new SmartMoveException("Failed to get vehicle status", e);
        }
    }

    /**
     * Create a new vehicle
     */
    public VehicleResponse createVehicle(CreateVehicleRequest request) {
        log.info("Creating vehicle: {}", request.getVehicleId());

        lockManager.lock(request.getVehicleId());

        try {
            // Check if vehicle already exists
            if (vehicleRepository.loadVehicleById(request.getVehicleId()) != null) {
                throw new SmartMoveException("Vehicle already exists: " + request.getVehicleId());
            }

            // Convert request to entity
            Vehicle vehicle = vehicleMapper.toEntity(request);

            // Save to repository
            vehicleRepository.saveVehicle(vehicle);

            log.info("Vehicle created successfully: {}", vehicle.getVehicleId());

            // Return response
            return vehicleMapper.toResponse(vehicle);

        } catch (Exception e) {
            log.error("Error creating vehicle: {}", e.getMessage());
            throw new SmartMoveException("Failed to create vehicle", e);
        } finally {
            lockManager.unlock(request.getVehicleId());
        }
    }

    /**
     * Update existing vehicle
     */
    public VehicleResponse updateVehicle(String vehicleId, UpdateVehicleRequest request) {
        log.info("Updating vehicle: {}", vehicleId);

        lockManager.lock(vehicleId);

        try {
            // Load existing vehicle
            Vehicle vehicle = vehicleRepository.loadVehicleById(vehicleId);

            if (vehicle == null) {
                throw new SmartMoveException("Vehicle not found: " + vehicleId);
            }

            // Update vehicle with request data
            if (request.getLatitude() != null && request.getLongitude() != null) {
                vehicle.setLocation(new com.smartmove.domain.entity.GPSLocation(
                        request.getLatitude(),
                        request.getLongitude()
                ));
            }

            if (request.getBatteryPercentage() != null) {
                vehicle.setBatteryPercentage(request.getBatteryPercentage());
            }

            if (request.getTemperatureCelsius() != null) {
                vehicle.setTemperature(request.getTemperatureCelsius());
            }

            if (request.getState() != null) {
                vehicle.setState(request.getState());
            }

            if (request.getLocked() != null) {
                vehicle.setLocked(request.getLocked());
            }

            // Save updated vehicle
            vehicleRepository.saveVehicle(vehicle);

            log.info("Vehicle updated successfully: {}", vehicleId);

            // Return response
            return vehicleMapper.toResponse(vehicle);

        } catch (Exception e) {
            log.error("Error updating vehicle {}: {}", vehicleId, e.getMessage());
            throw new SmartMoveException("Failed to update vehicle", e);
        } finally {
            lockManager.unlock(vehicleId);
        }
    }

    /**
     * Delete a vehicle
     *
     * @param vehicleId The vehicle ID to delete
     */
    public void deleteVehicle(String vehicleId) {
        log.info("Deleting vehicle: {}", vehicleId);

        lockManager.lock(vehicleId);

        try {
            vehicleRepository.deleteVehicle(vehicleId);
            log.info("Vehicle deleted successfully: {}", vehicleId);
        } catch (Exception e) {
            log.error("Error deleting vehicle {}: {}", vehicleId, e.getMessage());
            throw new SmartMoveException("Failed to delete vehicle", e);
        } finally {
            lockManager.unlock(vehicleId);
        }
    }

    /**
     * Get total count of vehicles
     *
     * @return Number of vehicles
     */
    public int getVehicleCount() {
        return vehicleRepository.getVehicleCount();
    }
}
