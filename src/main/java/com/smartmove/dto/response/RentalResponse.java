package com.smartmove.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class RentalResponse {

    @JsonProperty("rental_id")
    private String rentalId;

    @JsonProperty("vehicle_id")
    private String vehicleId;

    @JsonProperty("user_id")
    private String userId;

    @JsonProperty("city")
    private String city;

    @JsonProperty("start_time")
    private LocalDateTime startTime;

    @JsonProperty("end_time")
    private LocalDateTime endTime;

    @JsonProperty("start_location")
    private LocationResponse startLocation;

    @JsonProperty("end_location")
    private LocationResponse endLocation;

    @JsonProperty("total_cost_amount")
    private BigDecimal totalCostAmount;

    @JsonProperty("cost_currency")
    private String costCurrency;

    @JsonProperty("completed")
    private boolean completed;

    @Data
    @Builder
    public static class LocationResponse {

        @JsonProperty("latitude")
        private double latitude;

        @JsonProperty("longitude")
        private double longitude;

        @JsonProperty("timestamp")
        private LocalDateTime timestamp;
    }
}
