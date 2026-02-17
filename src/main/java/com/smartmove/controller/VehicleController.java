package com.smartmove.controller;

/**
 * @author jniyi
 * @project smartmove-core-engine - 2026
 * @created 17.02.2026
 */

import com.smartmove.dto.request.CreateVehicleRequest;
import com.smartmove.dto.request.UpdateVehicleRequest;
import com.smartmove.dto.response.ApiResponse;
import com.smartmove.dto.response.VehicleListResponse;
import com.smartmove.dto.response.VehicleResponse;
import com.smartmove.service.VehicleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * VehicleController - REST API endpoints for vehicle management
 *
 * Provides HTTP endpoints for:
 * - Get all vehicles
 * - Get vehicles by city or state
 * - Get vehicle status details
 * - Create new vehicles
 * - Update vehicle information
 * - Delete vehicles
 *
 * All responses are wrapped in ApiResponse<T> for consistency.
 *
 */
@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
@Slf4j
public class VehicleController {

    private final VehicleService vehicleService;

    /**
     * Get all vehicles
     *
     * @return List of all vehicles in lightweight format
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<VehicleListResponse>>> getAllVehicles() {
        log.info("GET /api/v1/vehicles - Get all vehicles");

        try {
            List<VehicleListResponse> vehicles = vehicleService.getAllVehicles();

            ApiResponse<List<VehicleListResponse>> response = ApiResponse.builder()
                    .success(true)
                    .message("Vehicles retrieved successfully")
                    .data(vehicles)
                    .timestamp(System.currentTimeMillis())
                    .build();

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting all vehicles: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Get vehicles by city
     *
     * @param city The city name (LONDON, ROME, STOCKHOLM, MILAN, BERLIN, PARIS)
     * @return List of vehicles in the city
     */
    @GetMapping("/city/{city}")
    public ResponseEntity<ApiResponse<List<VehicleListResponse>>> getVehiclesByCity(
            @PathVariable String city) {
        log.info("GET /api/v1/vehicles/city/{} - Get vehicles by city", city);

        try {
            List<VehicleListResponse> vehicles = vehicleService.getVehiclesByCity(city);

            ApiResponse<List<VehicleListResponse>> response = ApiResponse.builder()
                    .success(true)
                    .message("Vehicles in " + city + " retrieved successfully")
                    .data(vehicles)
                    .timestamp(System.currentTimeMillis())
                    .build();

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting vehicles by city: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Get vehicles by state
     *
     * @param state The vehicle state (AVAILABLE, RESERVED, IN_USE, MAINTENANCE, EMERGENCY_LOCK, RELOCATING)
     * @return List of vehicles in that state
     */
    @GetMapping("/state/{state}")
    public ResponseEntity<ApiResponse<List<VehicleListResponse>>> getVehiclesByState(
            @PathVariable String state) {
        log.info("GET /api/v1/vehicles/state/{} - Get vehicles by state", state);

        try {
            List<VehicleListResponse> vehicles = vehicleService.getVehiclesByState(state);

            ApiResponse<List<VehicleListResponse>> response = ApiResponse.builder()
                    .success(true)
                    .message("Vehicles with state " + state + " retrieved successfully")
                    .data(vehicles)
                    .timestamp(System.currentTimeMillis())
                    .build();

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting vehicles by state: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Get detailed vehicle status/information
     *
     * @param vehicleId The vehicle ID
     * @return Detailed vehicle information
     */
    @GetMapping("/{vehicleId}")
    public ResponseEntity<ApiResponse<VehicleResponse>> getVehicleStatus(
            @PathVariable String vehicleId) {
        log.info("GET /api/v1/vehicles/{} - Get vehicle status", vehicleId);

        try {
            VehicleResponse vehicle = vehicleService.getVehicleStatus(vehicleId);

            ApiResponse<VehicleResponse> response = ApiResponse.builder()
                    .success(true)
                    .message("Vehicle status retrieved successfully")
                    .data(vehicle)
                    .timestamp(System.currentTimeMillis())
                    .build();

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting vehicle status: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Create a new vehicle
     *
     * Requires admin role. Accepts CreateVehicleRequest with:
     * - vehicleId (unique)
     * - type (BICYCLE, ELECTRIC_SCOOTER, MOPED)
     * - city (LONDON, ROME, STOCKHOLM, MILAN, BERLIN, PARIS)
     * - latitude and longitude
     * - Type-specific fields (wheelSize, motorPowerWatts, engineCcm, etc.)
     *
     * @param request The create vehicle request
     * @return Created vehicle information
     */
    @PostMapping("/admin/create")
    public ResponseEntity<ApiResponse<VehicleResponse>> createVehicle(
            @Valid @RequestBody CreateVehicleRequest request) {
        log.info("POST /api/v1/vehicles/admin/create - Create vehicle: {}", request.getVehicleId());

        try {
            VehicleResponse vehicle = vehicleService.createVehicle(request);

            ApiResponse<VehicleResponse> response = ApiResponse.builder()
                    .success(true)
                    .message("Vehicle created successfully")
                    .data(vehicle)
                    .timestamp(System.currentTimeMillis())
                    .build();

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(response);
        } catch (Exception e) {
            log.error("Error creating vehicle: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Update an existing vehicle
     *
     * Allows partial updates. Only provided fields are updated:
     * - latitude/longitude (location)
     * - batteryPercentage
     * - temperatureCelsius
     * - state (vehicle state)
     * - locked (lock status)
     *
     * @param vehicleId The vehicle ID to update
     * @param request The update request
     * @return Updated vehicle information
     */
    @PatchMapping("/{vehicleId}")
    public ResponseEntity<ApiResponse<VehicleResponse>> updateVehicle(
            @PathVariable String vehicleId,
            @Valid @RequestBody UpdateVehicleRequest request) {
        log.info("PATCH /api/v1/vehicles/{} - Update vehicle", vehicleId);

        try {
            VehicleResponse vehicle = vehicleService.updateVehicle(vehicleId, request);

            ApiResponse<VehicleResponse> response = ApiResponse.builder()
                    .success(true)
                    .message("Vehicle updated successfully")
                    .data(vehicle)
                    .timestamp(System.currentTimeMillis())
                    .build();

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error updating vehicle: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Delete a vehicle
     *
     * Requires admin role. Deletes vehicle completely from system.
     *
     * @param vehicleId The vehicle ID to delete
     * @return Success confirmation
     */
    @DeleteMapping("/{vehicleId}")
    public ResponseEntity<ApiResponse<Void>> deleteVehicle(
            @PathVariable String vehicleId) {
        log.info("DELETE /api/v1/vehicles/{} - Delete vehicle", vehicleId);

        try {
            vehicleService.deleteVehicle(vehicleId);

            ApiResponse<Void> response = ApiResponse.builder()
                    .success(true)
                    .message("Vehicle deleted successfully")
                    .timestamp(System.currentTimeMillis())
                    .build();

            return ResponseEntity
                    .status(HttpStatus.NO_CONTENT)
                    .body(response);
        } catch (Exception e) {
            log.error("Error deleting vehicle: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Get vehicle count
     *
     * Returns total number of vehicles in system.
     *
     * @return Vehicle count
     */
    @GetMapping("/admin/count")
    public ResponseEntity<ApiResponse<Integer>> getVehicleCount() {
        log.info("GET /api/v1/vehicles/admin/count - Get vehicle count");

        try {
            int count = vehicleService.getVehicleCount();

            ApiResponse<Integer> response = ApiResponse.builder()
                    .success(true)
                    .message("Vehicle count retrieved successfully")
                    .data(count)
                    .timestamp(System.currentTimeMillis())
                    .build();

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting vehicle count: {}", e.getMessage());
            throw e;
        }
    }
}
