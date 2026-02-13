package com.smartmove.controller;

import com.smartmove.core.Vehicle;
import com.smartmove.core.VehicleState;
import com.smartmove.core.VehicleType;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SmartMoveCentralControllerTest {

    @Test
    public void reserve_movesAvailableToReserved() {
        Vehicle v = new Vehicle("V1", VehicleType.E_SCOOTER);
        SmartMoveCentralController c = new SmartMoveCentralController();

        c.reserve(v);

        assertEquals(VehicleState.RESERVED, v.getState());
    }

    @Test(expected = IllegalStateException.class)
    public void reserve_failsIfNotAvailable() {
        Vehicle v = new Vehicle("V1", VehicleType.E_SCOOTER);
        v.setState(VehicleState.MAINTENANCE);

        new SmartMoveCentralController().reserve(v);
    }

    @Test
    public void cancelReservation_movesReservedToAvailable() {
        Vehicle v = new Vehicle("V1", VehicleType.BICYCLE);
        v.setState(VehicleState.RESERVED);

        SmartMoveCentralController c = new SmartMoveCentralController();
        c.cancelReservation(v);

        assertEquals(VehicleState.AVAILABLE, v.getState());
    }

    @Test(expected = IllegalStateException.class)
    public void cancelReservation_failsIfNotReserved() {
        Vehicle v = new Vehicle("V1", VehicleType.BICYCLE);

        new SmartMoveCentralController().cancelReservation(v);
    }
    
    @Test
    public void startRental_movesReservedToInUse() {
        Vehicle v = new Vehicle("V1", VehicleType.E_SCOOTER);
        v.setState(VehicleState.RESERVED);

        SmartMoveCentralController c = new SmartMoveCentralController();
        c.startRental(v);

        assertEquals(VehicleState.IN_USE, v.getState());
    }

    @Test(expected = IllegalStateException.class)
    public void startRental_failsIfNotReserved() {
        Vehicle v = new Vehicle("V1", VehicleType.E_SCOOTER);

        new SmartMoveCentralController().startRental(v);
    }

    @Test
    public void endRental_movesInUseToAvailable() {
        Vehicle v = new Vehicle("V1", VehicleType.BICYCLE);
        v.setState(VehicleState.IN_USE);

        SmartMoveCentralController c = new SmartMoveCentralController();
        c.endRental(v);

        assertEquals(VehicleState.AVAILABLE, v.getState());
    }

    @Test(expected = IllegalStateException.class)
    public void endRental_failsIfNotInUse() {
        Vehicle v = new Vehicle("V1", VehicleType.BICYCLE);
        v.setState(VehicleState.RESERVED);

        new SmartMoveCentralController().endRental(v);
    }
}
