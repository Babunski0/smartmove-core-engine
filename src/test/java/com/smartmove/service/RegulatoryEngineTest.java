package com.smartmove.service;

import com.smartmove.domain.entity.Bicycle;
import com.smartmove.domain.entity.ElectricScooter;
import com.smartmove.domain.entity.GPSLocation;
import com.smartmove.domain.entity.Moped;
import com.smartmove.domain.entity.Vehicle;
import com.smartmove.domain.enums.City;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RegulatoryEngineTest {

    private static final double PRICE_PER_KM = 1.50;
    private static final double DELTA = 1e-6;

    private RegulatoryEngine engine;

    @BeforeEach
    void setUp() {
        engine = new RegulatoryEngine();
    }

    @Test
    void costCalculation_LONDON_appliesMultiplier() {
        Vehicle bike = bicycleIn(City.LONDON);

        double cost = engine.calculateFinalCost(bike, City.LONDON, 10.0, 60);
        double expected = expectedCost(bike, 10.0, 60, 1.3);

        assertEquals(expected, cost, DELTA);
    }

    @Test
    void costCalculation_ROME_multiplier1_0() {
        Vehicle bike = bicycleIn(City.ROME);

        double cost = engine.calculateFinalCost(bike, City.ROME, 10.0, 60);
        double expected = expectedCost(bike, 10.0, 60, 1.0);

        assertEquals(expected, cost, DELTA);
    }

    @Test
    void costCalculation_STOCKHOLM_multiplier1_2() {
        Vehicle bike = bicycleIn(City.STOCKHOLM);

        double cost = engine.calculateFinalCost(bike, City.STOCKHOLM, 10.0, 60);
        double expected = expectedCost(bike, 10.0, 60, 1.2);

        assertEquals(expected, cost, DELTA);
    }

    @Test
    void costCalculation_BERLIN_multiplier0_95() {
        Vehicle bike = bicycleIn(City.BERLIN);

        double cost = engine.calculateFinalCost(bike, City.BERLIN, 10.0, 60);
        double expected = expectedCost(bike, 10.0, 60, 0.95);

        assertEquals(expected, cost, DELTA);
    }

    @Test
    void costCalculation_PARIS_multiplier1_25() {
        Vehicle bike = bicycleIn(City.PARIS);

        double cost = engine.calculateFinalCost(bike, City.PARIS, 10.0, 60);
        double expected = expectedCost(bike, 10.0, 60, 1.25);

        assertEquals(expected, cost, DELTA);
    }

    @Test
    void costCalculation_MILAN_multiplier1_1() {
        Vehicle bike = bicycleIn(City.MILAN);

        double cost = engine.calculateFinalCost(bike, City.MILAN, 10.0, 60);
        double expected = expectedCost(bike, 10.0, 60, 1.1);

        assertEquals(expected, cost, DELTA);
    }

    @Test
    void distanceBasedPricing_durationZero() {
        Vehicle scooter = scooterIn(City.ROME);

        double cost = engine.calculateFinalCost(scooter, City.ROME, 12.0, 0);
        double expected = expectedCost(scooter, 12.0, 0, 1.0);

        assertEquals(expected, cost, DELTA);
        assertEquals(12.0 * PRICE_PER_KM, cost, DELTA);
    }

    @Test
    void durationBasedPricing_distanceZero() {
        Vehicle scooter = scooterIn(City.ROME);

        double cost = engine.calculateFinalCost(scooter, City.ROME, 0.0, 90);
        double expected = expectedCost(scooter, 0.0, 90, 1.0);

        assertEquals(expected, cost, DELTA);

        double hours = 90 / 60.0;
        assertEquals(hours * scooter.getHourlyRate(), cost, DELTA);
    }

    @Test
    void combinedDistanceAndDurationPricing() {
        Vehicle moped = mopedIn(City.PARIS);

        double cost = engine.calculateFinalCost(moped, City.PARIS, 7.5, 40);
        double expected = expectedCost(moped, 7.5, 40, 1.25);

        assertEquals(expected, cost, DELTA);
    }

    @Test
    void differentVehicleTypes_affectHourlyComponent() {
        double distance = 5.0;
        long duration = 60; // 1 hour
        City city = City.ROME;

        Vehicle bike = bicycleIn(city);
        Vehicle scooter = scooterIn(city);
        Vehicle moped = mopedIn(city);

        double bikeCost = engine.calculateFinalCost(bike, city, distance, duration);
        double scooterCost = engine.calculateFinalCost(scooter, city, distance, duration);
        double mopedCost = engine.calculateFinalCost(moped, city, distance, duration);

        assertTrue(bikeCost < scooterCost);
        assertTrue(scooterCost < mopedCost);
    }

    @Test
    void zeroDistanceCalculation_onlyDurationMatters() {
        Vehicle bike = bicycleIn(City.LONDON);

        double cost = engine.calculateFinalCost(bike, City.LONDON, 0.0, 30);
        double expected = expectedCost(bike, 0.0, 30, 1.3);

        assertEquals(expected, cost, DELTA);
    }

    @Test
    void longDistanceCalculation() {
        Vehicle moped = mopedIn(City.BERLIN);

        double cost = engine.calculateFinalCost(moped, City.BERLIN, 120.0, 0);
        double expected = expectedCost(moped, 120.0, 0, 0.95);

        assertEquals(expected, cost, DELTA);
    }

    @Test
    void shortDurationCalculation() {
        Vehicle scooter = scooterIn(City.MILAN);

        double cost = engine.calculateFinalCost(scooter, City.MILAN, 0.0, 5);
        double expected = expectedCost(scooter, 0.0, 5, 1.1);

        assertEquals(expected, cost, DELTA);
    }

    @Test
    void longDurationCalculation() {
        Vehicle bike = bicycleIn(City.STOCKHOLM);

        double cost = engine.calculateFinalCost(bike, City.STOCKHOLM, 0.0, 6 * 60);
        double expected = expectedCost(bike, 0.0, 6 * 60, 1.2);

        assertEquals(expected, cost, DELTA);
    }

    @Test
    void currencyRetrievalByCity() {
        assertEquals("GBP", engine.getCurrencyForCity(City.LONDON));
        assertEquals("SEK", engine.getCurrencyForCity(City.STOCKHOLM));

        assertEquals("EUR", engine.getCurrencyForCity(City.ROME));
        assertEquals("EUR", engine.getCurrencyForCity(City.BERLIN));
        assertEquals("EUR", engine.getCurrencyForCity(City.PARIS));
        assertEquals("EUR", engine.getCurrencyForCity(City.MILAN));
    }

    @Test
    void multiplierRetrievalByCity() {
        assertEquals(1.3, engine.getCityMultiplier(City.LONDON), DELTA);
        assertEquals(1.0, engine.getCityMultiplier(City.ROME), DELTA);
        assertEquals(1.2, engine.getCityMultiplier(City.STOCKHOLM), DELTA);
        assertEquals(1.1, engine.getCityMultiplier(City.MILAN), DELTA);
        assertEquals(0.95, engine.getCityMultiplier(City.BERLIN), DELTA);
        assertEquals(1.25, engine.getCityMultiplier(City.PARIS), DELTA);
    }

    private static double expectedCost(Vehicle vehicle, double distanceKm, long durationMinutes, double multiplier) {
        double distanceCost = distanceKm * PRICE_PER_KM;
        double hours = durationMinutes / 60.0;
        double hourlyCost = hours * vehicle.getHourlyRate();
        return (distanceCost + hourlyCost) * multiplier;
    }

    private static GPSLocation loc() {
        return new GPSLocation(0.0, 0.0, System.currentTimeMillis());
    }

    private static Vehicle bicycleIn(City city) {
        return new Bicycle("bike-1", city, loc());
    }

    private static Vehicle scooterIn(City city) {
        return new ElectricScooter("scooter-1", city, loc());
    }

    private static Vehicle mopedIn(City city) {
        return new Moped("moped-1", city, loc());
    }
}
