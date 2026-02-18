package com.smartmove.service;

import com.smartmove.domain.enums.VehicleState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class StateTransitionValidatorTest {

    private StateTransitionValidator validator;

    @BeforeEach
    void setUp() {
        validator = new StateTransitionValidator();
    }


    @Test
    void available_to_reserved_valid() {
        assertTrue(validator.isValidTransition(VehicleState.AVAILABLE, VehicleState.RESERVED));
    }

    @Test
    void reserved_to_inUse_valid() {
        assertTrue(validator.isValidTransition(VehicleState.RESERVED, VehicleState.IN_USE));
    }

    @Test
    void inUse_to_available_valid() {
        assertTrue(validator.isValidTransition(VehicleState.IN_USE, VehicleState.AVAILABLE));
    }

    @Test
    void inUse_to_emergencyLock_valid() {
        assertTrue(validator.isValidTransition(VehicleState.IN_USE, VehicleState.EMERGENCY_LOCK));
    }

    @Test
    void maintenance_to_available_valid() {
        assertTrue(validator.isValidTransition(VehicleState.MAINTENANCE, VehicleState.AVAILABLE));
    }

    @Test
    void emergencyLock_to_maintenance_valid() {
        assertTrue(validator.isValidTransition(VehicleState.EMERGENCY_LOCK, VehicleState.MAINTENANCE));
    }

    @Test
    void relocating_to_available_valid() {
        assertTrue(validator.isValidTransition(VehicleState.RELOCATING, VehicleState.AVAILABLE));
    }


    @Test
    void maintenance_to_inUse_invalid() {
        assertFalse(validator.isValidTransition(VehicleState.MAINTENANCE, VehicleState.IN_USE));
    }

    @Test
    void emergencyLock_to_available_invalid() {
        assertFalse(validator.isValidTransition(VehicleState.EMERGENCY_LOCK, VehicleState.AVAILABLE));
    }

    @Test
    void reserved_to_emergencyLock_invalid() {
        assertFalse(validator.isValidTransition(VehicleState.RESERVED, VehicleState.EMERGENCY_LOCK));
    }

    @Test
    void null_state_invalid() {
        assertFalse(validator.isValidTransition(null, VehicleState.AVAILABLE));
        assertFalse(validator.isValidTransition(VehicleState.AVAILABLE, null));
    }


    @Test
    void same_state_is_valid() {
        for (VehicleState state : VehicleState.values()) {
            assertTrue(validator.isValidTransition(state, state));
        }
    }


    @Test
    void validateTransition_throws_on_invalid() {
        assertThrows(IllegalStateException.class,
                () -> validator.validateTransition(VehicleState.MAINTENANCE, VehicleState.IN_USE));
    }

    @Test
    void validateTransition_does_not_throw_on_valid() {
        assertDoesNotThrow(() ->
                validator.validateTransition(VehicleState.AVAILABLE, VehicleState.RESERVED));
    }


    @Test
    void getValidNextStates_returns_expected_set() {
        Set<VehicleState> next = validator.getValidNextStates(VehicleState.AVAILABLE);

        assertTrue(next.contains(VehicleState.RESERVED));
        assertTrue(next.contains(VehicleState.IN_USE));
        assertTrue(next.contains(VehicleState.MAINTENANCE));
        assertTrue(next.contains(VehicleState.RELOCATING));
    }


    @Test
    void canBeRented_logic() {
        assertTrue(validator.canBeRented(VehicleState.AVAILABLE));
        assertTrue(validator.canBeRented(VehicleState.RESERVED));
        assertFalse(validator.canBeRented(VehicleState.MAINTENANCE));
    }

    @Test
    void isAvailable_logic() {
        assertTrue(validator.isAvailable(VehicleState.AVAILABLE));
        assertFalse(validator.isAvailable(VehicleState.RESERVED));
    }

    @Test
    void isInUse_logic() {
        assertTrue(validator.isInUse(VehicleState.IN_USE));
        assertFalse(validator.isInUse(VehicleState.AVAILABLE));
    }


    @Test
    void test_all_transitions_from_each_state() {
        // Define which transitions are valid for your state machine
        Map<VehicleState, Set<VehicleState>> validTransitions = new EnumMap<>(VehicleState.class);

        // AVAILABLE: has valid transitions to RESERVED, IN_USE, MAINTENANCE, RELOCATING
        validTransitions.put(VehicleState.AVAILABLE, Set.of(
                VehicleState.AVAILABLE,  // Same state
                VehicleState.RESERVED,   // available_to_reserved_valid
                VehicleState.IN_USE,     // Inferred from reserved_to_inUse_valid
                VehicleState.MAINTENANCE,
                VehicleState.RELOCATING));

        // IN_USE: can go to AVAILABLE or EMERGENCY_LOCK (NOT MAINTENANCE)
        validTransitions.put(VehicleState.IN_USE, Set.of(
                VehicleState.IN_USE,           // Same state
                VehicleState.AVAILABLE,        // inUse_to_available_valid
                VehicleState.EMERGENCY_LOCK)); // inUse_to_emergencyLock_valid (NOT MAINTENANCE)

        // MAINTENANCE: can only go to AVAILABLE (NOT RELOCATING)
        validTransitions.put(VehicleState.MAINTENANCE, Set.of(
                VehicleState.MAINTENANCE,  // Same state
                VehicleState.AVAILABLE)); // maintenance_to_available_valid (NOT RELOCATING)

        // EMERGENCY_LOCK: can go to MAINTENANCE (NOT AVAILABLE)
        validTransitions.put(VehicleState.EMERGENCY_LOCK, Set.of(
                VehicleState.EMERGENCY_LOCK,  // Same state
                VehicleState.MAINTENANCE));  // emergencyLock_to_maintenance_valid (NOT AVAILABLE)

        // RELOCATING: can go to AVAILABLE
        validTransitions.put(VehicleState.RELOCATING, Set.of(
                VehicleState.RELOCATING,  // Same state
                VehicleState.AVAILABLE)); // relocating_to_available_valid

        // RESERVED: can go to IN_USE or AVAILABLE
        validTransitions.put(VehicleState.RESERVED, Set.of(
                VehicleState.RESERVED,  // Same state
                VehicleState.IN_USE,    // reserved_to_inUse_valid
                VehicleState.AVAILABLE)); // RESERVED -> AVAILABLE is valid

        for (VehicleState current : VehicleState.values()) {
            for (VehicleState next : VehicleState.values()) {
                boolean isValid = validator.isValidTransition(current, next);
                Set<VehicleState> validNextStates = validTransitions.getOrDefault(current, Collections.emptySet());
                boolean shouldBeValid = validNextStates.contains(next);

                assertEquals(shouldBeValid, isValid,
                        "Transition from " + current + " to " + next + " validity mismatch");
            }
        }
    }
}
