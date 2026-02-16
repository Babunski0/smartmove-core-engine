package com.smartmove.mapper;

import com.smartmove.domain.entity.GPSLocation;
import com.smartmove.domain.entity.Vehicle;
import com.smartmove.dto.response.VehicleListResponse;
import com.smartmove.dto.response.VehicleResponse;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class VehicleMapper {

    public VehicleResponse toResponse(Vehicle vehicle) {
        GPSLocation loc = vehicle.getCurrentLocation();

        return VehicleResponse.builder()
                .vehicleId(vehicle.getVehicleId())
                .type(vehicle.getType() != null ? vehicle.getType().name() : null)
                .city(vehicle.getCity() != null ? vehicle.getCity().name() : null)
                .state(vehicle.getCurrentState() != null ? vehicle.getCurrentState().name() : null)
                .locked(vehicle.isLocked())
                .latitude(loc != null ? loc.getLatitude() : 0.0)
                .longitude(loc != null ? loc.getLongitude() : 0.0)
                .locationTimestamp(loc != null ? Instant.ofEpochMilli(loc.getTimestamp()) : null)
                .batteryPercentage((int) Math.round(vehicle.getBatteryPercentage()))
                .temperatureCelsius(vehicle.getTemperatureCelsius())
                .activeRentalId(vehicle.getActiveRentalId())
                .hourlyRate(vehicle.getHourlyRate())
                .maintenanceIntervalHours(vehicle.getMaintenanceIntervalHours())
                .requiredMaintenanceChecks(vehicle.getRequiredMaintenanceChecks() != null
                ? vehicle.getRequiredMaintenanceChecks().size()
                : 0)
                .build();
    }

    public VehicleListResponse toListResponse(Vehicle vehicle) {
        return VehicleListResponse.builder()
                .vehicleId(vehicle.getVehicleId())
                .type(vehicle.getType() != null ? vehicle.getType().name() : null)
                .city(vehicle.getCity() != null ? vehicle.getCity().name() : null)
                .state(vehicle.getCurrentState() != null ? vehicle.getCurrentState().name() : null)
                .batteryPercentage((int) Math.round(vehicle.getBatteryPercentage()))
                .locked(vehicle.isLocked())
                .hourlyRate(vehicle.getHourlyRate())
                .build();
    }
}


    // Request DTO → Entity (Create)
    // NOTE: Require ElectricScooter and Moped classes

    /*public Vehicle toEntity(CreateVehicleRequest request) {
        GPSLocation location = new GPSLocation(request.getLatitude(), request.getLongitude());

        return switch (request.getType()) {
            case BICYCLE -> new Bicycle(request.getVehicleId(), request.getCity(), location);
            case ELECTRIC_SCOOTER -> new ElectricScooter(request.getVehicleId(), request.getCity(), location);
            case MOPED -> new Moped(request.getVehicleId(), request.getCity(), location);
        };
    }*/