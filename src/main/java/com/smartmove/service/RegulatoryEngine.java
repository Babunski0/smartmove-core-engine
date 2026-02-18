package com.smartmove.service;


import com.smartmove.domain.entity.Vehicle;
import com.smartmove.domain.enums.City;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


/**
 * @author jniyi
 * @project smartmove-core-engine - 2026
 * @created 17.02.2026
 */

/**
 * RegulatoryEngine - Handles city-specific pricing and rules
 *
 * Implements business rules for:
 * - City-specific pricing
 * - Distance-based calculations
 * - Time-based charges
 * - Additional fees and taxes
 * - Cost calculations
 */
@Component
@Slf4j
public class RegulatoryEngine {
    /**
     * Base price per kilometer for rentals
     */
    private static final double PRICE_PER_KM = 1.50;

    /**
     * City-specific price multipliers
     */
    private static final double LONDON_MULTIPLIER = 1.3;   // 30% more expensive
    private static final double ROME_MULTIPLIER = 1.0;     // Standard price
    private static final double STOCKHOLM_MULTIPLIER = 1.2; // 20% more expensive
    private static final double MILAN_MULTIPLIER = 1.1;    // 10% more expensive
    private static final double BERLIN_MULTIPLIER = 0.95;  // 5% cheaper
    private static final double PARIS_MULTIPLIER = 1.25;   // 25% more expensive

    /**
     * Calculate final rental cost based on distance, time, and city
     *
     * Formula: (basePrice * distance + hourlyRate * hours) * cityMultiplier
     *
     * @param vehicle The vehicle being rented
     * @param city The city where rental takes place
     * @param distanceKm Distance traveled in kilometers
     * @param durationMinutes Duration of rental in minutes
     * @return Calculated rental cost
     */
    public double calculateFinalCost(Vehicle vehicle, City city, double distanceKm, long durationMinutes) {
        log.debug("Calculating cost for {} km, {} minutes in {}", distanceKm, durationMinutes, city);

        // Calculate base cost from distance
        double distanceCost = distanceKm * PRICE_PER_KM;

        // Calculate hourly cost
        double hours = durationMinutes / 60.0;
        double hourlyCost = hours * vehicle.getHourlyRate();

        // Total base cost (distance + hourly)
        double baseCost = distanceCost + hourlyCost;

        // Apply city multiplier
        double cityMultiplier = getCityMultiplier(city);
        double finalCost = baseCost * cityMultiplier;

        log.debug("Cost breakdown - Distance: €{}, Hourly: €{}, Base: €{}, Final: €{}",
                String.format("%.2f", distanceCost),
                String.format("%.2f", hourlyCost),
                String.format("%.2f", baseCost),
                String.format("%.2f", finalCost));

        return finalCost;
    }

    /**
     * Get city-specific price multiplier
     *
     * @param city The city
     * @return Price multiplier for the city
     */
    public double getCityMultiplier(City city) {
        return switch (city) {
            case LONDON -> LONDON_MULTIPLIER;
            case ROME -> ROME_MULTIPLIER;
            case STOCKHOLM -> STOCKHOLM_MULTIPLIER;
            case MILAN -> MILAN_MULTIPLIER;
            case BERLIN -> BERLIN_MULTIPLIER;
            case PARIS -> PARIS_MULTIPLIER;
            default -> 1.0;
        };
    }

    /**
     * Get city name for display
     *
     * @param city The city enum
     * @return Human-readable city name
     */
    public String getCityName(City city) {
        return city != null ? city.toString() : "Unknown";
    }

    /**
     * Get currency for city
     *
     * @param city The city
     * @return Currency code (EUR, GBP, SEK)
     */
    public String getCurrencyForCity(City city) {
        return switch (city) {
            case LONDON -> "GBP";
            case STOCKHOLM -> "SEK";
            default -> "EUR";
        };
    }

    /**
     * Calculate minimum rental cost
     *
     * @param vehicle The vehicle
     * @return Minimum cost for a rental (e.g., 1 hour minimum)
     */
    public double getMinimumRentalCost(Vehicle vehicle) {
        return vehicle.getHourlyRate();
    }
}
