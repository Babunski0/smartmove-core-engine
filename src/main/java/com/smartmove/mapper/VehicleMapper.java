package com.smartmove.mapper;

import com.smartmove.domain.entity.Bicycle;
import com.smartmove.domain.entity.GPSLocation;
import com.smartmove.domain.entity.Vehicle;
import com.smartmove.dto.request.CreateVehicleRequest;
import com.smartmove.dto.response.VehicleListResponse;
import com.smartmove.dto.response.VehicleResponse;
import org.springframework.stereotype.Component;

/**
 * @author jniyi
 * @project smartmove-core-engine - 2026
 * @created 14.02.2026
 */

@Component
public class VehicleMapper {

    // Entity → Response DTO
    public VehicleResponse toResponse(Vehicle vehicle) {
        return VehicleResponse.builder()
                .vehicleId(vehicle.getVehicleId())
                .type(vehicle.getType())
                .city(vehicle.getCity())
                .state(vehicle.getCurrentState())
                .locked(vehicle.isLocked())
                .latitude(vehicle.getCurrentLocation().getLatitude())
                .longitude(vehicle.getCurrentLocation().getLongitude())
                .locationTimestamp(vehicle.getCurrentLocation().getTimestamp())
                .batteryPercentage(vehicle.getBatteryPercentage())
                .temperatureCelsius(vehicle.getTemperatureCelsius())
                .activeRentalId(vehicle.getActiveRentalId())
                .hourlyRate(vehicle.getHourlyRate())
                .maintenanceIntervalHours(vehicle.getMaintenanceIntervalHours())
                .requiredMaintenanceChecks(vehicle.getRequiredMaintenanceChecks())
                .build();
    }

    // List Entity → List Response DTO
    public VehicleListResponse toListResponse(Vehicle vehicle) {
        return VehicleListResponse.builder()
                .vehicleId(vehicle.getVehicleId())
                .type(vehicle.getType())
                .city(vehicle.getCity())
                .state(vehicle.getCurrentState())
                .batteryPercentage(vehicle.getBatteryPercentage())
                .locked(vehicle.isLocked())
                .hourlyRate(vehicle.getHourlyRate())
                .build();
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
}
