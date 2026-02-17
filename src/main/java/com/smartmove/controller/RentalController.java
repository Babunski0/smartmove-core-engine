package com.smartmove.controller;

import com.smartmove.dto.request.EndRentalRequest;
import com.smartmove.dto.request.ReserveVehicleRequest;
import com.smartmove.dto.request.StartRentalRequest;
import com.smartmove.dto.response.ApiResponse;
import com.smartmove.dto.response.RentalResponse;
import com.smartmove.service.RentalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


/**
 * @author jniyi
 * @project smartmove-core-engine - 2026
 * @created 17.02.2026
 */


/**
 * RentalController - REST API endpoints for rental management
 *
 * Provides HTTP endpoints for:
 * - Reserve vehicles
 * - Start rentals
 * - End rentals with cost calculation
 *
 * All responses are wrapped in ApiResponse<T> for consistency.
 *
 */
@RestController
@RequestMapping("/api/rentals")
@RequiredArgsConstructor
@Slf4j
public class RentalController {

    private final RentalService rentalService;

    /**
     * Reserve a vehicle for a user
     *
     * Creates a rental reservation with RESERVED state.
     * Vehicle must be available.
     * User must exist.
     *
     * @param request The reserve vehicle request with vehicleId, userId, city
     * @return Created rental response
     */
    @PostMapping("/reserve")
    public ResponseEntity<ApiResponse<RentalResponse>> reserveVehicle(
            @Valid @RequestBody ReserveVehicleRequest request) {
        log.info("POST /api/rentals/reserve - Reserve vehicle {} for user {}",
                request.getVehicleId(), request.getUserId());

        try {
            RentalResponse rental = rentalService.reserveVehicle(request);

            ApiResponse<RentalResponse> response = new ApiResponse<>(
                    true,
                    "Vehicle reserved successfully",
                    rental
            );

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(response);
        } catch (Exception e) {
            log.error("Error reserving vehicle: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Start a rental
     *
     * Changes rental state to IN_USE.
     * Vehicle must be in RESERVED state.
     * Records start time and location.
     *
     * @param rentalId The rental ID
     * @param request The start rental request with vehicleId
     * @return Updated rental response
     */
    @PostMapping("/{rentalId}/start")
    public ResponseEntity<ApiResponse<RentalResponse>> startRental(
            @PathVariable String rentalId,
            @Valid @RequestBody StartRentalRequest request) {
        log.info("POST /api/rentals/{}/start - Start rental for vehicle {}",
                rentalId, request.getVehicleId());

        try {
            RentalResponse rental = rentalService.startRental(rentalId, request);

            ApiResponse<RentalResponse> response = new ApiResponse<>(
                    true,
                    "Rental started successfully",
                    rental
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error starting rental {}: {}", rentalId, e.getMessage());
            throw e;
        }
    }

    /**
     * End a rental and calculate cost
     *
     * Completes the rental with end time and location.
     * Calculates cost based on distance, duration, city, and vehicle.
     * Updates vehicle to AVAILABLE state.
     *
     * @param rentalId The rental ID
     * @param vehicleId The vehicle ID
     * @param request The end rental request with end location (latitude, longitude)
     * @return Completed rental response with calculated cost
     */
    @PostMapping("/{rentalId}/end/{vehicleId}")
    public ResponseEntity<ApiResponse<RentalResponse>> endRental(
            @PathVariable String rentalId,
            @PathVariable String vehicleId,
            @Valid @RequestBody EndRentalRequest request) {
        log.info("POST /api/v1/rentals/{}/end/{} - End rental", rentalId, vehicleId);

        try {
            RentalResponse rental = rentalService.endRental(rentalId, vehicleId, request);

            ApiResponse<RentalResponse> response = new ApiResponse<>(
                    true,
                    "Rental ended successfully",
                    rental
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error ending rental {}: {}", rentalId, e.getMessage());
            throw e;
        }
    }
}
