package com.smartmove.controller;

import com.smartmove.dto.response.VehicleListResponse;
import com.smartmove.dto.response.VehicleResponse;
import com.smartmove.exception.LockAcquisitionException;
import com.smartmove.exception.SmartMoveException;
import com.smartmove.exception.handler.GlobalExceptionHandler;
import com.smartmove.service.VehicleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = VehicleController.class)
@Import(GlobalExceptionHandler.class)
class VehicleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @org.springframework.boot.test.mock.mockito.MockBean
    private VehicleService vehicleService;

    @Test
    void getAllVehicles_returnsOkApiResponse() throws Exception {
        var v1 = VehicleListResponse.builder().vehicleId("V1").build();
        var v2 = VehicleListResponse.builder().vehicleId("V2").build();

        when(vehicleService.getAllVehicles()).thenReturn(List.of(v1, v2));

        mockMvc.perform(get("/api/vehicles"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Vehicles retrieved successfully"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].vehicleId").value("V1"))
                .andExpect(jsonPath("$.data[1].vehicleId").value("V2"));
    }

    @Test
    void getVehiclesByCity_returnsOkApiResponse() throws Exception {
        var v1 = VehicleListResponse.builder().vehicleId("V1").build();
        when(vehicleService.getVehiclesByCity("LONDON")).thenReturn(List.of(v1));

        mockMvc.perform(get("/api/vehicles/city/LONDON"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Vehicles in LONDON retrieved successfully"))
                .andExpect(jsonPath("$.data[0].vehicleId").value("V1"));
    }

    @Test
    void getVehiclesByState_returnsOkApiResponse() throws Exception {
        var v1 = VehicleListResponse.builder().vehicleId("V1").build();
        when(vehicleService.getVehiclesByState("AVAILABLE")).thenReturn(List.of(v1));

        mockMvc.perform(get("/api/vehicles/state/AVAILABLE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Vehicles with state AVAILABLE retrieved successfully"))
                .andExpect(jsonPath("$.data[0].vehicleId").value("V1"));
    }

    @Test
    void getVehicleStatus_returnsOkApiResponse() throws Exception {
        var resp = VehicleResponse.builder()
                .vehicleId("V1")
                .type("BICYCLE")
                .city("LONDON")
                .state("AVAILABLE")
                .locked(true)
                .latitude(51.5)
                .longitude(-0.1)
                .batteryPercentage(90.0)
                .temperatureCelsius(20.0)
                .build();

        when(vehicleService.getVehicleStatus("V1")).thenReturn(resp);

        mockMvc.perform(get("/api/vehicles/V1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Vehicle status retrieved successfully"))
                .andExpect(jsonPath("$.data.vehicleId").value("V1"))
                .andExpect(jsonPath("$.data.city").value("LONDON"));
    }

    @Test
    void createVehicle_returnsCreatedApiResponse() throws Exception {
        var resp = VehicleResponse.builder()
                .vehicleId("V1")
                .type("BICYCLE")
                .city("LONDON")
                .state("AVAILABLE")
                .locked(true)
                .latitude(51.5)
                .longitude(-0.1)
                .batteryPercentage(100.0)
                .temperatureCelsius(25.0)
                .build();

        when(vehicleService.createVehicle(any())).thenReturn(resp);

        String json = """
                {
                  "vehicleId": "V1",
                  "type": "BICYCLE",
                  "city": "LONDON",
                  "latitude": 51.5,
                  "longitude": -0.1
                }
                """;

        mockMvc.perform(post("/api/vehicles/admin/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Vehicle created successfully"))
                .andExpect(jsonPath("$.data.vehicleId").value("V1"));
    }

    @Test
    void createVehicle_whenValidationFails_returns400FromHandler() throws Exception {
        // Missing required fields like city/latitude/longitude/type -> MethodArgumentNotValidException -> 400
        String invalidJson = """
                {
                  "vehicleId": "V1"
                }
                """;

        mockMvc.perform(post("/api/vehicles/admin/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Request validation failed. Please check the field errors"))
                .andExpect(jsonPath("$.data.error").value("Validation Failed"));
    }

    @Test
    void updateVehicle_returnsOkApiResponse() throws Exception {
        var resp = VehicleResponse.builder()
                .vehicleId("V1")
                .type("BICYCLE")
                .city("LONDON")
                .state("IN_USE")
                .locked(false)
                .latitude(51.6)
                .longitude(-0.12)
                .batteryPercentage(55.0)
                .temperatureCelsius(12.0)
                .build();

        when(vehicleService.updateVehicle(eq("V1"), any())).thenReturn(resp);

        String json = """
                {
                  "vehicleId": "V1",
                  "latitude": 51.6,
                  "longitude": -0.12,
                  "batteryPercentage": 55.0,
                  "temperatureCelsius": 12.0,
                  "state": "IN_USE",
                  "locked": false
                }
                """;

        mockMvc.perform(patch("/api/vehicles/V1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Vehicle updated successfully"))
                .andExpect(jsonPath("$.data.vehicleId").value("V1"))
                .andExpect(jsonPath("$.data.state").value("IN_USE"));
    }

    @Test
    void deleteVehicle_returnsNoContentApiResponse() throws Exception {
        doNothing().when(vehicleService).deleteVehicle("V1");

        mockMvc.perform(delete("/api/vehicles/V1"))
                .andExpect(status().isNoContent())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Vehicle deleted successfully"));
    }

    @Test
    void getVehicleCount_returnsOkApiResponse() throws Exception {
        when(vehicleService.getVehicleCount()).thenReturn(7);

        mockMvc.perform(get("/api/vehicles/admin/count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Vehicle count retrieved successfully"))
                .andExpect(jsonPath("$.data").value(7));
    }

    @Test
    void whenServiceThrowsSmartMoveException_returns400FromHandler() throws Exception {
        when(vehicleService.getVehicleStatus("missing"))
                .thenThrow(new SmartMoveException("Vehicle not found: missing"));

        mockMvc.perform(get("/api/vehicles/missing"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Vehicle not found: missing"))
                .andExpect(jsonPath("$.data.status").value(400))
                .andExpect(jsonPath("$.data.error").value("Bad Request"));
    }

    @Test
    void whenServiceThrowsLockAcquisitionException_returns503WithRetryAfter() throws Exception {
        when(vehicleService.getAllVehicles())
                .thenThrow(new LockAcquisitionException("Could not lock vehicle"));

        mockMvc.perform(get("/api/vehicles"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(header().string("Retry-After", "5"))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Resource is temporarily locked, please retry"))
                .andExpect(jsonPath("$.data.status").value(503))
                .andExpect(jsonPath("$.data.error").value("Service Unavailable"));
    }
}
