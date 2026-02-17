package com.smartmove.service;

/**
 * @author jniyi
 * @project smartmove-core-engine - 2026
 * @created 17.02.2026
 */

import com.smartmove.domain.enums.VehicleState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * StateTransitionValidator - Validates vehicle state transitions
 *
 * Ensures that vehicle state changes follow valid business rules.
 * Defines which states can transition to which other states.
 *
 * Valid transitions:
 * - AVAILABLE → RESERVED, IN_USE, MAINTENANCE
 * - RESERVED → IN_USE, AVAILABLE
 * - IN_USE → AVAILABLE, EMERGENCY_LOCK
 * - MAINTENANCE → AVAILABLE
 * - EMERGENCY_LOCK → MAINTENANCE
 * - RELOCATING → AVAILABLE
 *
 */
@Component
@Slf4j
public class StateTransitionValidator {

    /**
     * Define valid state transitions
     * Key: Current state, Value: Set of valid next states
     */
    private static final Map<VehicleState, Set<VehicleState>> VALID_TRANSITIONS = Map.of(
            VehicleState.AVAILABLE, new HashSet<>(Set.of(
                    VehicleState.RESERVED,
                    VehicleState.IN_USE,
                    VehicleState.MAINTENANCE,
                    VehicleState.RELOCATING
            )),
            VehicleState.RESERVED, new HashSet<>(Set.of(
                    VehicleState.IN_USE,
                    VehicleState.AVAILABLE
            )),
            VehicleState.IN_USE, new HashSet<>(Set.of(
                    VehicleState.AVAILABLE,
                    VehicleState.EMERGENCY_LOCK
            )),
            VehicleState.MAINTENANCE, new HashSet<>(Set.of(
                    VehicleState.AVAILABLE
            )),
            VehicleState.EMERGENCY_LOCK, new HashSet<>(Set.of(
                    VehicleState.MAINTENANCE
            )),
            VehicleState.RELOCATING, new HashSet<>(Set.of(
                    VehicleState.AVAILABLE
            ))
    );

    /**
     * Check if a state transition is valid
     *
     * @param currentState Current vehicle state
     * @param newState Desired new state
     * @return true if transition is valid, false otherwise
     */
    public boolean isValidTransition(VehicleState currentState, VehicleState newState) {
        log.debug("Validating transition from {} to {}", currentState, newState);

        if (currentState == null || newState == null) {
            log.warn("Invalid state transition: null state");
            return false;
        }

        // Same state is valid (no-op)
        if (currentState == newState) {
            return true;
        }

        Set<VehicleState> validNextStates = VALID_TRANSITIONS.get(currentState);
        if (validNextStates == null) {
            log.warn("No valid transitions defined for state: {}", currentState);
            return false;
        }

        boolean isValid = validNextStates.contains(newState);
        log.debug("Transition valid: {}", isValid);
        return isValid;
    }

    /**
     * Validate a state transition, throwing exception if invalid
     *
     * @param currentState Current vehicle state
     * @param newState Desired new state
     * @throws IllegalStateException if transition is invalid
     */
    public void validateTransition(VehicleState currentState, VehicleState newState) {
        if (!isValidTransition(currentState, newState)) {
            log.error("Invalid state transition attempt: {} → {}", currentState, newState);
            throw new IllegalStateException(
                    String.format("Cannot transition from %s to %s", currentState, newState)
            );
        }
    }

    /**
     * Get valid next states for a given state
     *
     * @param currentState The current vehicle state
     * @return Set of valid next states
     */
    public Set<VehicleState> getValidNextStates(VehicleState currentState) {
        return VALID_TRANSITIONS.getOrDefault(currentState, new HashSet<>());
    }

    /**
     * Check if vehicle can be rented (is in AVAILABLE or RESERVED state)
     *
     * @param state The vehicle state
     * @return true if vehicle can be rented
     */
    public boolean canBeRented(VehicleState state) {
        return state == VehicleState.AVAILABLE || state == VehicleState.RESERVED;
    }

    /**
     * Check if vehicle is available for new reservation
     *
     * @param state The vehicle state
     * @return true if vehicle is available
     */
    public boolean isAvailable(VehicleState state) {
        return state == VehicleState.AVAILABLE;
    }

    /**
     * Check if vehicle is in use
     *
     * @param state The vehicle state
     * @return true if vehicle is in use
     */
    public boolean isInUse(VehicleState state) {
        return state == VehicleState.IN_USE;
    }
}
