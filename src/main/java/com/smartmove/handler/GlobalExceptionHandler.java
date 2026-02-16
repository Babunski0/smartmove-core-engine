package com.smartmove.handler;

import com.smartmove.exception.LockAcquisitionException;
import com.smartmove.exception.RentalNotFoundException;
import com.smartmove.exception.SmartMoveException;
import com.smartmove.exception.VehicleNotAvailableException;
import com.smartmove.dto.response.ApiResponse;
import com.smartmove.dto.response.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.LinkedHashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(SmartMoveException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleSmartMoveException(SmartMoveException ex) {
        log.warn("SmartMoveException: {}", ex.getMessage());
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), null);
    }

    @ExceptionHandler(VehicleNotAvailableException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleVehicleNotAvailable(VehicleNotAvailableException ex) {
        log.warn("VehicleNotAvailableException: {}", ex.getMessage());
        return build(HttpStatus.CONFLICT, ex.getMessage(), null);
    }

    @ExceptionHandler(RentalNotFoundException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleRentalNotFound(RentalNotFoundException ex) {
        log.warn("RentalNotFoundException: {}", ex.getMessage());
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), null);
    }

    @ExceptionHandler(LockAcquisitionException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleLockAcquisition(LockAcquisitionException ex) {
        log.error("LockAcquisitionException: {}", ex.getMessage());
        return build(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage(), null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(fe.getField(), fe.getDefaultMessage());
        }

        log.warn("Validation failed: {}", fieldErrors);

        ErrorResponse error = ErrorResponse.builder()
                .error("Validation failed")
                .fieldErrors(fieldErrors)
                .build();

        return build(HttpStatus.BAD_REQUEST, "Invalid request", error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleGeneric(Exception ex) {
        // Do not expose stack trace to client
        log.error("Unhandled exception", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error", null);
    }

    private ResponseEntity<ApiResponse<ErrorResponse>> build(HttpStatus status,
                                                            String message,
                                                            ErrorResponse error) {
        ApiResponse<ErrorResponse> body = ApiResponse.<ErrorResponse>builder()
                .success(false)
                .message(message)
                .data(error)
                .build();

        return ResponseEntity.status(status).body(body);
    }
}
