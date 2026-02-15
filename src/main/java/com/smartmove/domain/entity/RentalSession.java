package com.smartmove.domain.entity;

import com.smartmove.domain.enums.City;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Data
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
    private Map<String, BigDecimal> additionalCharges = new HashMap<>();

    public void endRental(GPSLocation endLocation) {
        if (this.completed) {
            return;
        }
        this.endLocation = endLocation;
        this.endTime = Instant.now();
        this.completed = true;
    }

    public void addCharge(String reason, double amount) {
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Charge reason must not be blank");
        }
        if (amount < 0) {
            throw new IllegalArgumentException("Charge amount must be >= 0");
        }
        additionalCharges.put(reason, BigDecimal.valueOf(amount));
    }

    public long getDurationMinutes() {
        Instant end = (endTime != null) ? endTime : Instant.now();
        Instant start = (startTime != null) ? startTime : end;
        return Math.max(0, (end.toEpochMilli() - start.toEpochMilli()) / 60000);
    }

    public boolean isCompleted() {
        return completed;
    }
}
