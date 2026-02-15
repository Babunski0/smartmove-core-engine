package com.smartmove.service.mapper;

import com.smartmove.domain.entity.GPSLocation;
import com.smartmove.domain.entity.RentalSession;
import com.smartmove.dto.response.RentalResponse;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Component
public class RentalMapper {

    public RentalResponse toResponse(RentalSession rental) {

        if (rental == null) {
            return null;
        }

        return RentalResponse.builder()
                .rentalId(rental.getRentalId())
                .vehicleId(rental.getVehicleId())
                .userId(rental.getUserId())
                .city(rental.getCity() != null ? rental.getCity().name() : null)
                .startTime(rental.getStartTime())
                .endTime(rental.getEndTime())
                .startLocation(mapLocation(rental.getStartLocation()))
                .endLocation(mapLocation(rental.getEndLocation()))
                .totalCostAmount(rental.getTotalCostAmount())
                .costCurrency(rental.getCostCurrency())
                .completed(rental.isCompleted())
                .build();
    }

    private RentalResponse.LocationResponse mapLocation(GPSLocation location) {

        if (location == null) {
            return null;
        }

        LocalDateTime convertedTime =
                Instant.ofEpochMilli(location.getTimestamp())
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime();

        return RentalResponse.LocationResponse.builder()
                .latitude(location.getLatitude())
                .longitude(location.getLongitude())
                .timestamp(convertedTime)
                .build();
    }
}
