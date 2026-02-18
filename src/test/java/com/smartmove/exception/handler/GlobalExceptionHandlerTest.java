package com.smartmove.exception.handler;

import com.smartmove.dto.response.ApiResponse;
import com.smartmove.dto.response.ErrorResponse;
import com.smartmove.exception.SmartMoveException;
import com.smartmove.exception.VehicleNotAvailableException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleSmartMoveException_returns400() {
        SmartMoveException ex = new SmartMoveException("boom");

        ResponseEntity<?> entity = handler.handleSmartMoveException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, entity.getStatusCode());

        ApiResponse<?> body = assertApiResponse(entity.getBody());
        assertFalse(body.isSuccess());
        assertEquals("boom", body.getMessage());
        assertNotNull(body.getTimestamp());

        ErrorResponse err = assertErrorResponse(body.getData());
        assertEquals(400, err.getStatus());
        assertEquals("Bad Request", err.getError());
        assertEquals("boom", err.getMessage());
        assertNotNull(err.getTimestamp());
    }

    @Test
    void handleVehicleNotAvailableException_returns400() {
        VehicleNotAvailableException ex = new VehicleNotAvailableException("vehicle not available");

        ResponseEntity<?> entity = handler.handleVehicleNotAvailableException(ex);

        assertEquals(HttpStatus.CONFLICT, entity.getStatusCode());

        ApiResponse<?> body = assertApiResponse(entity.getBody());
        assertFalse(body.isSuccess());
        assertEquals("Vehicle is not available for this operation", body.getMessage());
        assertNotNull(body.getTimestamp());

        ErrorResponse err = assertErrorResponse(body.getData());
        assertEquals(409, err.getStatus());
        assertEquals("Conflict", err.getError());
        assertEquals("vehicle not available", err.getMessage());
        assertNotNull(err.getTimestamp());
    }

    @Test
    void handleValidationException_returns400() {
        MethodArgumentNotValidException ex = buildValidationException(
                Map.of(
                        "email", "must be a well-formed email address",
                        "firstName", "must not be blank"
                )
        );

        ResponseEntity<?> entity = handler.handleMethodArgumentNotValidException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, entity.getStatusCode());

        ApiResponse<?> body = assertApiResponse(entity.getBody());
        assertFalse(body.isSuccess());
        assertEquals("Request validation failed. Please check the field errors", body.getMessage());
        assertNotNull(body.getTimestamp());

        ErrorResponse err = assertErrorResponse(body.getData());
        assertEquals(400, err.getStatus());
        assertEquals("Validation Failed", err.getError());
        assertEquals("Request validation failed", err.getMessage());
        assertNotNull(err.getTimestamp());
        assertNotNull(err.getFieldErrors());

        assertEquals("must be a well-formed email address", err.getFieldErrors().get("email"));
        assertEquals("must not be blank", err.getFieldErrors().get("firstName"));
    }

    @Test
    void handleGenericException_returns500() {
        Exception ex = new RuntimeException("kaboom");

        ResponseEntity<?> entity = handler.handleGenericException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, entity.getStatusCode());

        ApiResponse<?> body = assertApiResponse(entity.getBody());
        assertFalse(body.isSuccess());
        assertEquals("An unexpected error occurred", body.getMessage());
        assertNotNull(body.getTimestamp());

        ErrorResponse err = assertErrorResponse(body.getData());
        assertEquals(500, err.getStatus());
        assertEquals("Internal Server Error", err.getError());
        assertEquals("An unexpected error occurred. Please contact support if the problem persists", err.getMessage());
        assertNotNull(err.getTimestamp());
    }

    @Test
    void errorMessages_inResponse() {
        SmartMoveException ex = new SmartMoveException("specific message");

        ResponseEntity<?> entity = handler.handleSmartMoveException(ex);

        ApiResponse<?> body = assertApiResponse(entity.getBody());
        ErrorResponse err = assertErrorResponse(body.getData());

        assertEquals("specific message", body.getMessage());
        assertEquals("specific message", err.getMessage());
    }

    @Test
    void fieldErrorHandling_includesFieldErrors() {
        MethodArgumentNotValidException ex = buildValidationException(
                Map.of("homeCity", "must not be null")
        );

        ResponseEntity<?> entity = handler.handleMethodArgumentNotValidException(ex);

        ApiResponse<?> body = assertApiResponse(entity.getBody());
        ErrorResponse err = assertErrorResponse(body.getData());

        assertNotNull(err.getFieldErrors());
        assertEquals(1, err.getFieldErrors().size());
        assertEquals("must not be null", err.getFieldErrors().get("homeCity"));
    }

    @Test
    void timestamp_inResponse() {
        long before = System.currentTimeMillis();

        ResponseEntity<?> entity = handler.handleGenericException(new Exception("x"));

        long after = System.currentTimeMillis();

        ApiResponse<?> body = assertApiResponse(entity.getBody());
        ErrorResponse err = assertErrorResponse(body.getData());

        assertTrue(body.getTimestamp() >= before && body.getTimestamp() <= after);
        assertTrue(err.getTimestamp() >= before && err.getTimestamp() <= after);
    }

    private static ApiResponse<?> assertApiResponse(Object body) {
        assertNotNull(body);
        assertTrue(body instanceof ApiResponse<?>);
        return (ApiResponse<?>) body;
    }

    private static ErrorResponse assertErrorResponse(Object data) {
        assertNotNull(data);
        assertTrue(data instanceof ErrorResponse);
        return (ErrorResponse) data;
    }

    private static MethodArgumentNotValidException buildValidationException(Map<String, String> fieldToMessage) {
        Object target = new Object();
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(target, "target");

        fieldToMessage.forEach((field, message) ->
                bindingResult.addError(new FieldError("target", field, message))
        );

        return new MethodArgumentNotValidException((org.springframework.core.MethodParameter) null, bindingResult);

    }
}
