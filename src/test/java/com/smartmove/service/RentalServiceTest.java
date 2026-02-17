package com.smartmove.service;

import com.smartmove.domain.entity.GPSLocation;
import com.smartmove.domain.entity.RentalSession;
import com.smartmove.domain.entity.User;
import com.smartmove.domain.entity.Vehicle;
import com.smartmove.domain.enums.City;
import com.smartmove.domain.enums.VehicleState;
import com.smartmove.dto.request.EndRentalRequest;
import com.smartmove.dto.request.ReserveVehicleRequest;
import com.smartmove.dto.request.StartRentalRequest;
import com.smartmove.dto.response.RentalResponse;
import com.smartmove.exception.SmartMoveException;
import com.smartmove.exception.VehicleNotAvailableException;
import com.smartmove.mapper.RentalMapper;
import com.smartmove.repository.FileBasedRentalRepository;
import com.smartmove.repository.FileBasedUserRepository;
import com.smartmove.repository.FileBasedVehicleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RentalServiceTest {

    @Mock private FileBasedVehicleRepository vehicleRepository;
    @Mock private FileBasedRentalRepository rentalRepository;
    @Mock private FileBasedUserRepository userRepository;
    @Mock private VehicleLockManager lockManager;
    @Mock private StateTransitionValidator stateValidator;
    @Mock private RegulatoryEngine regulatoryEngine;
    @Mock private RentalMapper rentalMapper;

    @InjectMocks
    private RentalService rentalService;

    @Test
    void reserveVehicle_happyPath_locks_updatesVehicle_savesRental_unlocks_andReturnsResponse() {
        ReserveVehicleRequest request = mock(ReserveVehicleRequest.class);
        when(request.getVehicleId()).thenReturn("V1");
        when(request.getUserId()).thenReturn("U1");
        when(request.getCity()).thenReturn(City.LONDON);

        User user = new User();
        user.setUserId("U1");
        user.setFirstName("John");
        user.setLastName("Doe");

        Vehicle vehicle = mock(Vehicle.class);
        GPSLocation currentLoc = new GPSLocation(51.5, -0.1);

        when(userRepository.loadUserById("U1")).thenReturn(user);
        when(vehicleRepository.loadVehicleById("V1")).thenReturn(vehicle);
        when(vehicle.getCurrentState()).thenReturn(VehicleState.AVAILABLE);
        when(vehicle.getVehicleId()).thenReturn("V1");
        when(vehicle.getCurrentLocation()).thenReturn(currentLoc);
        when(stateValidator.isValidTransition(VehicleState.AVAILABLE, VehicleState.RESERVED)).thenReturn(true);

        RentalResponse response = RentalResponse.builder().build();
        when(rentalMapper.toResponse(any(RentalSession.class))).thenReturn(response);

        RentalResponse result = rentalService.reserveVehicle(request);

        assertSame(response, result);

        InOrder inOrder = inOrder(lockManager, userRepository, vehicleRepository, rentalRepository, rentalMapper);
        inOrder.verify(lockManager).lock("V1");
        inOrder.verify(userRepository).loadUserById("U1");
        inOrder.verify(vehicleRepository).loadVehicleById("V1");

        // not in InOrder, but we verify it's called
        verify(stateValidator).isValidTransition(VehicleState.AVAILABLE, VehicleState.RESERVED);

        inOrder.verify(vehicleRepository).saveVehicle(vehicle);
        inOrder.verify(rentalRepository).saveRental(any(RentalSession.class));
        inOrder.verify(rentalMapper).toResponse(any(RentalSession.class));
        inOrder.verify(lockManager).unlock("V1");

        verify(vehicle).setState(VehicleState.RESERVED);
        verify(vehicle).setActiveRentalId(anyString());
    }

    @Test
    void reserveVehicle_whenUserNotFound_throws_andUnlocks() {
        ReserveVehicleRequest request = mock(ReserveVehicleRequest.class);
        when(request.getVehicleId()).thenReturn("V1");
        when(request.getUserId()).thenReturn("U404");

        when(userRepository.loadUserById("U404")).thenReturn(null);

        SmartMoveException ex = assertThrows(SmartMoveException.class, () -> rentalService.reserveVehicle(request));
        assertTrue(ex.getMessage().contains("User not found"));

        verify(lockManager).lock("V1");
        verify(userRepository).loadUserById("U404");
        verify(lockManager).unlock("V1");

        verifyNoInteractions(vehicleRepository, rentalRepository, stateValidator, regulatoryEngine, rentalMapper);
    }

    @Test
    void reserveVehicle_whenVehicleNotFound_throwsVehicleNotAvailable_andUnlocks() {
        ReserveVehicleRequest request = mock(ReserveVehicleRequest.class);
        when(request.getVehicleId()).thenReturn("V404");
        when(request.getUserId()).thenReturn("U1");

        User user = new User();
        user.setUserId("U1");

        when(userRepository.loadUserById("U1")).thenReturn(user);
        when(vehicleRepository.loadVehicleById("V404")).thenReturn(null);

        VehicleNotAvailableException ex = assertThrows(
                VehicleNotAvailableException.class,
                () -> rentalService.reserveVehicle(request)
        );
        assertTrue(ex.getMessage().contains("Vehicle not found"));

        verify(lockManager).lock("V404");
        verify(lockManager).unlock("V404");
        verify(rentalRepository, never()).saveRental(any());
        verify(vehicleRepository, never()).saveVehicle(any());
    }

    @Test
    void reserveVehicle_whenInvalidStateTransition_throwsVehicleNotAvailable_andUnlocks() {
        ReserveVehicleRequest request = mock(ReserveVehicleRequest.class);
        when(request.getVehicleId()).thenReturn("V1");
        when(request.getUserId()).thenReturn("U1");

        User user = new User();
        user.setUserId("U1");

        Vehicle vehicle = mock(Vehicle.class);

        when(userRepository.loadUserById("U1")).thenReturn(user);
        when(vehicleRepository.loadVehicleById("V1")).thenReturn(vehicle);
        when(vehicle.getCurrentState()).thenReturn(VehicleState.IN_USE);
        when(stateValidator.isValidTransition(VehicleState.IN_USE, VehicleState.RESERVED)).thenReturn(false);

        VehicleNotAvailableException ex = assertThrows(
                VehicleNotAvailableException.class,
                () -> rentalService.reserveVehicle(request)
        );
        assertTrue(ex.getMessage().contains("Cannot reserve vehicle"));

        verify(lockManager).lock("V1");
        verify(lockManager).unlock("V1");
        verify(vehicleRepository, never()).saveVehicle(any());
        verify(rentalRepository, never()).saveRental(any());
    }

    @Test
    void reserveVehicle_whenLockFails_throws_andDoesNotUnlockOrCallDeps() {
        ReserveVehicleRequest request = mock(ReserveVehicleRequest.class);
        when(request.getVehicleId()).thenReturn("V1");

        doThrow(new SmartMoveException("Could not lock vehicle: V1"))
                .when(lockManager).lock("V1");

        SmartMoveException ex = assertThrows(SmartMoveException.class, () -> rentalService.reserveVehicle(request));
        assertEquals("Could not lock vehicle: V1", ex.getMessage());

        verify(lockManager).lock("V1");
        verify(lockManager, never()).unlock(anyString());
        verifyNoInteractions(vehicleRepository, rentalRepository, userRepository, stateValidator, regulatoryEngine, rentalMapper);
    }

    @Test
    void startRental_happyPath_updatesVehicleAndRental_unlocks_andReturnsResponse() {
        StartRentalRequest request = mock(StartRentalRequest.class);
        when(request.getVehicleId()).thenReturn("V1");

        User rentalUser = new User();
        rentalUser.setUserId("U1");

        RentalSession rental = mock(RentalSession.class);
        when(rental.getUser()).thenReturn(rentalUser);

        User user = new User();
        user.setUserId("U1");

        Vehicle vehicle = mock(Vehicle.class);
        GPSLocation loc = new GPSLocation(51.5, -0.1);

        when(rentalRepository.loadRentalById("R1")).thenReturn(rental);
        when(userRepository.loadUserById("U1")).thenReturn(user);
        when(vehicleRepository.loadVehicleById("V1")).thenReturn(vehicle);
        when(vehicle.getCurrentState()).thenReturn(VehicleState.RESERVED);
        when(vehicle.getCurrentLocation()).thenReturn(loc);
        when(stateValidator.isValidTransition(VehicleState.RESERVED, VehicleState.IN_USE)).thenReturn(true);

        RentalResponse response = RentalResponse.builder().build();
        when(rentalMapper.toResponse(rental)).thenReturn(response);

        RentalResponse result = rentalService.startRental("R1", request);

        assertSame(response, result);

        InOrder inOrder = inOrder(lockManager, rentalRepository, userRepository, vehicleRepository, rentalMapper);
        inOrder.verify(lockManager).lock("V1");
        inOrder.verify(rentalRepository).loadRentalById("R1");
        inOrder.verify(userRepository).loadUserById("U1");
        inOrder.verify(vehicleRepository).loadVehicleById("V1");
        inOrder.verify(vehicleRepository).saveVehicle(vehicle);
        inOrder.verify(rentalRepository).saveRental(rental);
        inOrder.verify(rentalMapper).toResponse(rental);
        inOrder.verify(lockManager).unlock("V1");

        verify(vehicle).setState(VehicleState.IN_USE);
        verify(vehicle).setLocked(false);
        verify(rental).setStartTime(any(Instant.class));
        verify(rental).setStartLocation(loc);
    }

    @Test
    void startRental_whenRentalNotFound_throws_andUnlocks() {
        StartRentalRequest request = mock(StartRentalRequest.class);
        when(request.getVehicleId()).thenReturn("V1");

        when(rentalRepository.loadRentalById("R404")).thenReturn(null);

        SmartMoveException ex = assertThrows(SmartMoveException.class, () -> rentalService.startRental("R404", request));
        assertTrue(ex.getMessage().contains("Rental not found") || ex.getCause() != null);

        verify(lockManager).lock("V1");
        verify(lockManager).unlock("V1");
        verify(vehicleRepository, never()).saveVehicle(any());
        verify(rentalRepository, never()).saveRental(any());
    }

    @Test
    void startRental_whenInvalidStateTransition_throwsVehicleNotAvailable_andUnlocks() {
        StartRentalRequest request = mock(StartRentalRequest.class);
        when(request.getVehicleId()).thenReturn("V1");

        User rentalUser = new User();
        rentalUser.setUserId("U1");

        RentalSession rental = mock(RentalSession.class);
        when(rental.getUser()).thenReturn(rentalUser);

        Vehicle vehicle = mock(Vehicle.class);

        when(rentalRepository.loadRentalById("R1")).thenReturn(rental);
        when(userRepository.loadUserById("U1")).thenReturn(new User());
        when(vehicleRepository.loadVehicleById("V1")).thenReturn(vehicle);
        when(vehicle.getCurrentState()).thenReturn(VehicleState.MAINTENANCE);
        when(stateValidator.isValidTransition(VehicleState.MAINTENANCE, VehicleState.IN_USE)).thenReturn(false);

        assertThrows(VehicleNotAvailableException.class, () -> rentalService.startRental("R1", request));

        verify(lockManager).lock("V1");
        verify(lockManager).unlock("V1");
        verify(vehicleRepository, never()).saveVehicle(any());
        verify(rentalRepository, never()).saveRental(any());
    }

    @Test
    void endRental_happyPath_calculatesCost_updatesRentalAndVehicle_unlocks_andReturnsResponse() {
        String rentalId = "R1";
        String vehicleId = "V1";

        EndRentalRequest request = mock(EndRentalRequest.class);
        when(request.getLatitude()).thenReturn(51.6);
        when(request.getLongitude()).thenReturn(-0.12);

        User rentalUser = new User();
        rentalUser.setUserId("U1");

        RentalSession rental = new RentalSession(
                rentalId,
                vehicleId,
                rentalUser,
                City.LONDON,
                new GPSLocation(51.5, -0.1)
        );
        rental.setStartTime(Instant.now().minusSeconds(600));
        rental.setEndTime(Instant.now());

        Vehicle vehicle = mock(Vehicle.class);

        when(rentalRepository.loadRentalById(rentalId)).thenReturn(rental);
        when(vehicleRepository.loadVehicleById(vehicleId)).thenReturn(vehicle);
        when(userRepository.loadUserById("U1")).thenReturn(rentalUser);

        when(regulatoryEngine.calculateFinalCost(eq(vehicle), eq(City.LONDON), anyDouble(), anyLong()))
                .thenReturn(12.345);
        when(regulatoryEngine.getCurrencyForCity(City.LONDON)).thenReturn("GBP");

        RentalResponse response = RentalResponse.builder().build();
        when(rentalMapper.toResponse(rental)).thenReturn(response);

        RentalResponse result = rentalService.endRental(rentalId, vehicleId, request);

        assertSame(response, result);

        verify(lockManager).lock(vehicleId);

        verify(rentalRepository).loadRentalById(rentalId);
        verify(vehicleRepository).loadVehicleById(vehicleId);
        verify(userRepository).loadUserById("U1");

        verify(regulatoryEngine).calculateFinalCost(eq(vehicle), eq(City.LONDON), anyDouble(), anyLong());
        verify(regulatoryEngine, atLeastOnce()).getCurrencyForCity(City.LONDON);

        verify(rentalRepository).saveRental(any(RentalSession.class));
        verify(vehicleRepository).saveVehicle(vehicle);
        verify(rentalMapper).toResponse(rental);

        verify(lockManager).unlock(vehicleId);

        verify(vehicle).setState(VehicleState.AVAILABLE);
        verify(vehicle).setLocked(true);
        verify(vehicle).setLocation(any(GPSLocation.class));
        verify(vehicle).setActiveRentalId(null);

        assertNotNull(rental.getTotalCostAmount());
        assertEquals("GBP", rental.getCostCurrency());
        assertTrue(rental.isCompleted());
        assertNotNull(rental.getEndLocation());
    }

    @Test
    void endRental_whenRentalNotFound_throws_andUnlocks() {
        EndRentalRequest request = mock(EndRentalRequest.class);

        when(rentalRepository.loadRentalById("R404")).thenReturn(null);

        SmartMoveException ex = assertThrows(SmartMoveException.class, () -> rentalService.endRental("R404", "V1", request));
        assertTrue(ex.getMessage().contains("Rental not found") || ex.getCause() != null);

        verify(lockManager).lock("V1");
        verify(lockManager).unlock("V1");
        verify(vehicleRepository, never()).saveVehicle(any());
        verify(rentalRepository, never()).saveRental(any());
    }

    @Test
    void endRental_whenRentalNotStarted_throws_andUnlocks() {
        String rentalId = "R1";
        String vehicleId = "V1";

        EndRentalRequest request = mock(EndRentalRequest.class);

        User rentalUser = new User();
        rentalUser.setUserId("U1");

        RentalSession rental = new RentalSession(rentalId, vehicleId, rentalUser, City.LONDON, new GPSLocation(51.5, -0.1));
        rental.setStartTime(null);

        when(rentalRepository.loadRentalById(rentalId)).thenReturn(rental);
        when(vehicleRepository.loadVehicleById(vehicleId)).thenReturn(mock(Vehicle.class));
        when(userRepository.loadUserById("U1")).thenReturn(rentalUser);

        SmartMoveException ex = assertThrows(SmartMoveException.class, () -> rentalService.endRental(rentalId, vehicleId, request));
        assertTrue(ex.getMessage().contains("Rental has not been started") || ex.getCause() != null);

        verify(lockManager).lock(vehicleId);
        verify(lockManager).unlock(vehicleId);
        verify(rentalRepository, never()).saveRental(any());
        verify(vehicleRepository, never()).saveVehicle(any());
    }

    @Test
    void endRental_whenLockFails_throws_andDoesNotCallDeps() {
        EndRentalRequest request = mock(EndRentalRequest.class);

        doThrow(new SmartMoveException("Could not lock vehicle: V1"))
                .when(lockManager).lock("V1");

        SmartMoveException ex = assertThrows(SmartMoveException.class, () -> rentalService.endRental("R1", "V1", request));
        assertEquals("Could not lock vehicle: V1", ex.getMessage());

        verify(lockManager).lock("V1");
        verify(lockManager, never()).unlock(anyString());
        verifyNoInteractions(vehicleRepository, rentalRepository, userRepository, stateValidator, regulatoryEngine, rentalMapper);
    }
}
