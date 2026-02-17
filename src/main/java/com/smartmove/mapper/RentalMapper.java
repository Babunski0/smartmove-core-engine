package com.smartmove.mapper;

import com.smartmove.domain.entity.RentalSession;
import com.smartmove.dto.response.RentalResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
@Slf4j
public class RentalMapper {

    /**
     * Convert RentalSession entity to RentalResponse DTO
     *
     * Maps all rental information including user and location data.
     *
     * @param rental The RentalSession entity
     * @return RentalResponse DTO for API response
     */
    public RentalResponse toResponse(RentalSession rental) {
        log.debug("Mapping rental to response: {}", rental.getRentalId());
        if (rental == null) {
            return null;
        }

        return RentalResponse.builder()
                .rentalId(rental.getRentalId())
                .vehicleId(rental.getVehicleId())
                .userId(rental.getUser().getUserId())  // Extract userId from User object
                .city(rental.getCity())
                .durationMinutes(rental.getDurationMinutes())
                .totalCostAmount(rental.getTotalCostAmount())
                .costCurrency(rental.getCostCurrency())
                .completed(rental.isCompleted())
                .build();
    }
}
