package com.smartmove.dto.response;

import com.smartmove.domain.enums.City;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class RentalResponse {

    private String rentalId;
    private String vehicleId;
    private String userId;
    private City city;
    private Long durationMinutes;
    private BigDecimal totalCostAmount;
    private String costCurrency;
    private Boolean completed;
}
