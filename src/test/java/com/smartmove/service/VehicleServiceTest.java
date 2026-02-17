package com.smartmove.service;

import com.smartmove.domain.entity.GPSLocation;
import com.smartmove.domain.entity.Vehicle;
import com.smartmove.domain.enums.City;
import com.smartmove.domain.enums.VehicleState;
import com.smartmove.domain.enums.VehicleType;
import com.smartmove.dto.request.CreateVehicleRequest;
import com.smartmove.dto.request.UpdateVehicleRequest;
import com.smartmove.dto.response.VehicleListResponse;
import com.smartmove.dto.response.VehicleResponse;
import com.smartmove.exception.SmartMoveException;
import com.smartmove.mapper.VehicleMapper;
import com.smartmove.repository.FileBasedVehicleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VehicleServiceTest {

    @Mock
    private FileBasedVehicleRepository vehicleRepository;

    @Mock
    private VehicleLockManager lockManager;

    @Mock
    private VehicleMapper vehicleMapper;

    @InjectMocks
    private VehicleService vehicleService;

    // Minimal concrete Vehicle implementation for update tests
    private static final class TestVehicle extends Vehicle {
        TestVehicle(String vehicleId, VehicleType type, City city, GPSLocation location) {
            super(vehicleId, type, city, location);
        }

        @Override
        public double getHourlyRate() {
            return 1.0;
        }

        @Override
        public int getMaintenanceIntervalHours() {
            return 100;
        }

        @Override
        public List<String> getRequiredMaintenanceChecks() {
            return List.of();
        }
    }

    @Test
    void getAllVehicles_returnsMappedResponses() {
        Vehicle v1 = mock(Vehicle.class);
        Vehicle v2 = mock(Vehicle.class);

        VehicleListResponse r1 = VehicleListResponse.builder().vehicleId("V1").build();
        VehicleListResponse r2 = VehicleListResponse.builder().vehicleId("V2").build();

        when(vehicleRepository.loadAllVehicles()).thenReturn(List.of(v1, v2));
        when(vehicleMapper.toListResponse(v1)).thenReturn(r1);
        when(vehicleMapper.toListResponse(v2)).thenReturn(r2);

        List<VehicleListResponse> result = vehicleService.getAllVehicles();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("V1", result.get(0).getVehicleId());
        assertEquals("V2", result.get(1).getVehicleId());

        verify(vehicleRepository).loadAllVehicles();
        verify(vehicleMapper).toListResponse(v1);
        verify(vehicleMapper).toListResponse(v2);
        verifyNoMoreInteractions(vehicleRepository, vehicleMapper);
        verifyNoInteractions(lockManager);
    }

    @Test
    void getAllVehicles_whenRepositoryThrows_wrapsInSmartMoveException() {
        when(vehicleRepository.loadAllVehicles()).thenThrow(new RuntimeException("boom"));

        SmartMoveException ex = assertThrows(SmartMoveException.class, () -> vehicleService.getAllVehicles());
        assertEquals("Failed to retrieve vehicles", ex.getMessage());
        assertNotNull(ex.getCause());

        verify(vehicleRepository).loadAllVehicles();
        verifyNoInteractions(vehicleMapper, lockManager);
    }

    @Test
    void getVehicleStatus_whenVehicleExists_returnsMappedResponse() {
        Vehicle vehicle = mock(Vehicle.class);
        VehicleResponse response = VehicleResponse.builder().vehicleId("V1").build();

        when(vehicleRepository.loadVehicleById("V1")).thenReturn(vehicle);
        when(vehicleMapper.toResponse(vehicle)).thenReturn(response);

        VehicleResponse result = vehicleService.getVehicleStatus("V1");

        assertNotNull(result);
        assertEquals("V1", result.getVehicleId());

        verify(vehicleRepository).loadVehicleById("V1");
        verify(vehicleMapper).toResponse(vehicle);
        verifyNoMoreInteractions(vehicleRepository, vehicleMapper);
        verifyNoInteractions(lockManager);
    }

    @Test
    void getVehicleStatus_whenVehicleNotFound_throwsWrappedSmartMoveException() {
        when(vehicleRepository.loadVehicleById("missing")).thenReturn(null);

        SmartMoveException ex = assertThrows(SmartMoveException.class, () -> vehicleService.getVehicleStatus("missing"));
        // Method catches and wraps all exceptions, including "Vehicle not found"
        assertEquals("Failed to get vehicle status", ex.getMessage());
        assertNotNull(ex.getCause());

        verify(vehicleRepository).loadVehicleById("missing");
        verifyNoInteractions(vehicleMapper, lockManager);
    }

    @Test
    void createVehicle_happyPath_locks_saves_unlocks_andReturnsResponse() {
        CreateVehicleRequest req = CreateVehicleRequest.builder()
                .vehicleId("V1")
                .type(VehicleType.BICYCLE)
                .city(City.LONDON) // use any existing enum value in your project
                .latitude(42.0)
                .longitude(19.0)
                .build();

        Vehicle vehicle = mock(Vehicle.class);
        when(vehicle.getVehicleId()).thenReturn("V1");

        VehicleResponse response = VehicleResponse.builder().vehicleId("V1").build();

        when(vehicleRepository.loadVehicleById("V1")).thenReturn(null);
        when(vehicleMapper.toEntity(req)).thenReturn(vehicle);
        when(vehicleMapper.toResponse(vehicle)).thenReturn(response);

        VehicleResponse result = vehicleService.createVehicle(req);

        assertNotNull(result);
        assertEquals("V1", result.getVehicleId());

        InOrder inOrder = inOrder(lockManager, vehicleRepository, vehicleMapper);
        inOrder.verify(lockManager).lock("V1");
        inOrder.verify(vehicleRepository).loadVehicleById("V1");
        inOrder.verify(vehicleMapper).toEntity(req);
        inOrder.verify(vehicleRepository).saveVehicle(vehicle);
        inOrder.verify(vehicleMapper).toResponse(vehicle);
        inOrder.verify(lockManager).unlock("V1");

        verifyNoMoreInteractions(vehicleRepository, vehicleMapper, lockManager);
    }

    @Test
    void createVehicle_whenVehicleAlreadyExists_throws_andDoesNotSave_butUnlocks() {
        CreateVehicleRequest req = CreateVehicleRequest.builder()
                .vehicleId("V1")
                .type(VehicleType.BICYCLE)
                .city(City.LONDON)
                .latitude(42.0)
                .longitude(19.0)
                .build();

        Vehicle existing = mock(Vehicle.class);
        when(vehicleRepository.loadVehicleById("V1")).thenReturn(existing);

        SmartMoveException ex = assertThrows(SmartMoveException.class, () -> vehicleService.createVehicle(req));
        assertEquals("Failed to create vehicle", ex.getMessage());
        assertNotNull(ex.getCause());

        verify(lockManager).lock("V1");
        verify(vehicleRepository).loadVehicleById("V1");
        verify(vehicleRepository, never()).saveVehicle(any());
        verify(vehicleMapper, never()).toEntity(any());
        verify(vehicleMapper, never()).toResponse(any());
        verify(lockManager).unlock("V1");
        verifyNoMoreInteractions(vehicleRepository, vehicleMapper, lockManager);
    }

    @Test
    void createVehicle_whenLockFails_throws_andDoesNotCallRepoOrUnlock() {
        CreateVehicleRequest req = CreateVehicleRequest.builder()
                .vehicleId("V1")
                .type(VehicleType.BICYCLE)
                .city(City.LONDON)
                .latitude(42.0)
                .longitude(19.0)
                .build();

        doThrow(new SmartMoveException("Could not lock vehicle: V1"))
                .when(lockManager).lock("V1");

        SmartMoveException ex = assertThrows(SmartMoveException.class, () -> vehicleService.createVehicle(req));
        assertEquals("Could not lock vehicle: V1", ex.getMessage());

        verify(lockManager).lock("V1");
        verifyNoInteractions(vehicleRepository, vehicleMapper);
        verify(lockManager, never()).unlock(anyString());
        verifyNoMoreInteractions(lockManager);
    }

    @Test
    void updateVehicle_happyPath_updatesFields_saves_unlocks_andReturnsResponse() {
        String vehicleId = "V1";

        UpdateVehicleRequest req = UpdateVehicleRequest.builder()
                .vehicleId(vehicleId)
                .latitude(42.1)
                .longitude(19.1)
                .batteryPercentage(55.0)
                .temperatureCelsius(12.0)
                .state(VehicleState.IN_USE)
                .locked(false)
                .build();

        Vehicle vehicle = new TestVehicle(vehicleId, VehicleType.BICYCLE, City.LONDON, new GPSLocation(42.0, 19.0));
        VehicleResponse response = VehicleResponse.builder().vehicleId(vehicleId).build();

        when(vehicleRepository.loadVehicleById(vehicleId)).thenReturn(vehicle);
        when(vehicleMapper.toResponse(vehicle)).thenReturn(response);

        VehicleResponse result = vehicleService.updateVehicle(vehicleId, req);

        assertNotNull(result);
        assertEquals(vehicleId, result.getVehicleId());

        ArgumentCaptor<Vehicle> captor = ArgumentCaptor.forClass(Vehicle.class);

        InOrder inOrder = inOrder(lockManager, vehicleRepository, vehicleMapper);
        inOrder.verify(lockManager).lock(vehicleId);
        inOrder.verify(vehicleRepository).loadVehicleById(vehicleId);
        inOrder.verify(vehicleRepository).saveVehicle(captor.capture());
        inOrder.verify(vehicleMapper).toResponse(vehicle);
        inOrder.verify(lockManager).unlock(vehicleId);

        Vehicle saved = captor.getValue();
        assertNotNull(saved.getCurrentLocation());
        assertEquals(42.1, saved.getCurrentLocation().getLatitude());
        assertEquals(19.1, saved.getCurrentLocation().getLongitude());
        assertEquals(55.0, saved.getBatteryPercentage());
        assertEquals(12.0, saved.getTemperatureCelsius());
        assertEquals(VehicleState.IN_USE, saved.getCurrentState());
        assertFalse(saved.isLocked());

        verifyNoMoreInteractions(vehicleRepository, vehicleMapper, lockManager);
    }

    @Test
    void updateVehicle_whenVehicleNotFound_throws_andDoesNotSave_butUnlocks() {
        String vehicleId = "missing";
        UpdateVehicleRequest req = UpdateVehicleRequest.builder()
                .vehicleId(vehicleId)
                .batteryPercentage(10.0)
                .build();

        when(vehicleRepository.loadVehicleById(vehicleId)).thenReturn(null);

        SmartMoveException ex = assertThrows(SmartMoveException.class, () -> vehicleService.updateVehicle(vehicleId, req));
        assertEquals("Failed to update vehicle", ex.getMessage());
        assertNotNull(ex.getCause());

        verify(lockManager).lock(vehicleId);
        verify(vehicleRepository).loadVehicleById(vehicleId);
        verify(vehicleRepository, never()).saveVehicle(any());
        verify(vehicleMapper, never()).toResponse(any());
        verify(lockManager).unlock(vehicleId);

        verifyNoMoreInteractions(vehicleRepository, vehicleMapper, lockManager);
    }

    @Test
    void updateVehicle_whenLockFails_throws_andDoesNotCallRepoOrUnlock() {
        String vehicleId = "V1";
        UpdateVehicleRequest req = UpdateVehicleRequest.builder().vehicleId(vehicleId).build();

        doThrow(new SmartMoveException("Could not lock vehicle: " + vehicleId))
                .when(lockManager).lock(vehicleId);

        SmartMoveException ex = assertThrows(SmartMoveException.class, () -> vehicleService.updateVehicle(vehicleId, req));
        assertEquals("Could not lock vehicle: " + vehicleId, ex.getMessage());

        verify(lockManager).lock(vehicleId);
        verifyNoInteractions(vehicleRepository, vehicleMapper);
        verify(lockManager, never()).unlock(anyString());
        verifyNoMoreInteractions(lockManager);
    }

    @Test
    void deleteVehicle_happyPath_locks_deletes_unlocks() {
        String vehicleId = "V1";

        vehicleService.deleteVehicle(vehicleId);

        InOrder inOrder = inOrder(lockManager, vehicleRepository);
        inOrder.verify(lockManager).lock(vehicleId);
        inOrder.verify(vehicleRepository).deleteVehicle(vehicleId);
        inOrder.verify(lockManager).unlock(vehicleId);

        verifyNoMoreInteractions(lockManager, vehicleRepository);
        verifyNoInteractions(vehicleMapper);
    }

    @Test
    void deleteVehicle_whenRepositoryThrows_wraps_andUnlocks() {
        String vehicleId = "V1";

        doThrow(new RuntimeException("io fail"))
                .when(vehicleRepository).deleteVehicle(vehicleId);

        SmartMoveException ex = assertThrows(SmartMoveException.class, () -> vehicleService.deleteVehicle(vehicleId));
        assertEquals("Failed to delete vehicle", ex.getMessage());
        assertNotNull(ex.getCause());

        verify(lockManager).lock(vehicleId);
        verify(vehicleRepository).deleteVehicle(vehicleId);
        verify(lockManager).unlock(vehicleId);

        verifyNoMoreInteractions(lockManager, vehicleRepository);
        verifyNoInteractions(vehicleMapper);
    }

    @Test
    void deleteVehicle_whenLockFails_throws_andDoesNotCallRepoOrUnlock() {
        String vehicleId = "V1";

        doThrow(new SmartMoveException("Could not lock vehicle: " + vehicleId))
                .when(lockManager).lock(vehicleId);

        SmartMoveException ex = assertThrows(SmartMoveException.class, () -> vehicleService.deleteVehicle(vehicleId));
        assertEquals("Could not lock vehicle: " + vehicleId, ex.getMessage());

        verify(lockManager).lock(vehicleId);
        verifyNoInteractions(vehicleRepository, vehicleMapper);
        verify(lockManager, never()).unlock(anyString());
        verifyNoMoreInteractions(lockManager);
    }
}
