package com.smartmove.mapper;

import com.smartmove.domain.entity.*;
import com.smartmove.dto.request.CreateVehicleRequest;
import com.smartmove.dto.response.VehicleListResponse;
import com.smartmove.dto.response.VehicleResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
@Slf4j
public class VehicleMapper {

    public VehicleResponse toResponse(Vehicle vehicle) {
        log.debug("Mapping vehicle to response: {}", vehicle.getVehicleId());

        if (vehicle == null) {
            return null;
        }

        return VehicleResponse.builder()
                .vehicleId(vehicle.getVehicleId())
                .type(vehicle.getType() != null ? vehicle.getType().name() : null)
                .city(vehicle.getCity() != null ? vehicle.getCity().name() : null)
                .state(vehicle.getCurrentState() != null ? vehicle.getCurrentState().name() : null)
                .locked(vehicle.isLocked())
                .latitude(vehicle.getCurrentLocation().getLatitude())
                .longitude(vehicle.getCurrentLocation().getLongitude())
                .batteryPercentage(vehicle.getBatteryPercentage())
                .temperatureCelsius(vehicle.getTemperatureCelsius())
                .locked(vehicle.isLocked())
                .build();
    }

    public VehicleListResponse toListResponse(Vehicle vehicle) {
        log.debug("Mapping vehicle to list response: {}", vehicle.getVehicleId());

        if (vehicle == null) {
            return null;
        }

        return VehicleListResponse.builder()
                .vehicleId(vehicle.getVehicleId())
                .type(vehicle.getType() != null ? vehicle.getType().name() : null)
                .city(vehicle.getCity() != null ? vehicle.getCity().name() : null)
                .state(vehicle.getCurrentState() != null ? vehicle.getCurrentState().name() : null)
                .batteryPercentage(vehicle.getBatteryPercentage())
                .locked(vehicle.isLocked())
                .hourlyRate(vehicle.getHourlyRate())
                .build();
    }

    // Request DTO → Entity (Create)
    public Vehicle toEntity(CreateVehicleRequest request) {
        log.debug("Mapping create request to entity: {}", request.getVehicleId());

        if (request == null) {
            return null;
        }

        // Create GPSLocation from request
        GPSLocation location = new GPSLocation(
                request.getLatitude(),
                request.getLongitude()
        );

        return switch (request.getType()) {
            case BICYCLE -> new Bicycle(request.getVehicleId(), request.getCity(), location);
            case ELECTRIC_SCOOTER -> new ElectricScooter(request.getVehicleId(), request.getCity(), location);
            case MOPED -> new Moped(request.getVehicleId(), request.getCity(), location);
        };
    }
}
