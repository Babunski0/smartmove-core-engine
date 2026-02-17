package com.smartmove.dto.response;

import com.smartmove.domain.enums.City;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RentalResponse {

    private String rentalId;
    private String vehicleId;
    private String userId;
    private City city;
    private Long durationMinutes;
    private Double totalCostAmount;
    private String costCurrency;
    private Boolean completed;
}
