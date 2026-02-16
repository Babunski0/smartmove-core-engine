package com.smartmove.domain.entity;

import com.smartmove.domain.enums.City;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RentalSession {

    private String rentalId;
    private String vehicleId;
    private String userId;
    private City city;

    private Instant startTime;
    private Instant endTime;

    private GPSLocation startLocation;
    private GPSLocation endLocation;

    private BigDecimal totalCostAmount;
    private String costCurrency;

    private boolean completed;
}
