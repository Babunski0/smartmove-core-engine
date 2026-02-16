package com.smartmove.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.smartmove.domain.enums.City;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
