package com.smartmove.domain.entity;

import com.smartmove.domain.enums.City;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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


    @Builder.Default
    private List<Map<String, Object>> additionalCharges = new ArrayList<>();

    public RentalSession(String rentalId, String vehicleId, String userId,
                         City city, GPSLocation startLocation) {
        this.rentalId = rentalId;
        this.vehicleId = vehicleId;
        this.userId = userId;
        this.city = city;
        this.startLocation = startLocation;
        this.startTime = Instant.now();
        this.additionalCharges = new ArrayList<>();
        this.completed = false;
    }

    public void endRental(GPSLocation endLocation) {
        this.endLocation = endLocation;
        this.endTime = Instant.now();
        this.completed = true;
    }

    public void addCharge(String chargeType, double amount) {
        Map<String, Object> charge = new HashMap<>();
        charge.put("type", chargeType);
        charge.put("amount", amount);
        charge.put("timestamp", Instant.now());
        additionalCharges.add(charge);
    }

    public long getDurationMinutes() {
        return Duration.between(endTime, startTime).toMinutes();
    }

    public Double getTotalAdditionalCharges() {
        return additionalCharges.stream()
                .mapToDouble(charge -> ((Number) charge.get("amount")).doubleValue())
                .sum();
    }
}
