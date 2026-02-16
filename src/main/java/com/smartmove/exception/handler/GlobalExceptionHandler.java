package com.smartmove.exception.handler;

import com.smartmove.exception.*;
import com.smartmove.dto.response.ApiResponse;
import com.smartmove.dto.response.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Handle SmartMoveException - Base custom exception
     *
     * HTTP Status: 400 Bad Request
     *
     * @param ex The exception
     * @return ApiResponse with error details
     */
    @ExceptionHandler(SmartMoveException.class)
    public ResponseEntity<?> handleSmartMoveException(SmartMoveException ex) {
        log.error("SmartMoveException occurred: {}", ex.getMessage(), ex);
        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Bad Request")
                .message(ex.getMessage())
                .timestamp(System.currentTimeMillis())
                .build();

        ApiResponse<?> response = ApiResponse.builder()
                .success(false)
                .message(ex.getMessage())
                .data(errorResponse)
                .timestamp(System.currentTimeMillis())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Handle VehicleNotAvailableException
     *
     * HTTP Status: 409 Conflict (vehicle not in correct state)
     *
     * @param ex The exception
     * @return ApiResponse with error details
     */
    @ExceptionHandler(VehicleNotAvailableException.class)
    public ResponseEntity<?> handleVehicleNotAvailableException(VehicleNotAvailableException ex) {
        log.warn("Vehicle not available: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(HttpStatus.CONFLICT.value())
                .error("Conflict")
                .message(ex.getMessage())
                .timestamp(System.currentTimeMillis())
                .build();

        ApiResponse<?> response = ApiResponse.builder()
                .success(false)
                .message("Vehicle is not available for this operation")
                .data(errorResponse)
                .timestamp(System.currentTimeMillis())
                .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    /**
     * Handle RentalNotFoundException
     *
     * HTTP Status: 404 Not Found
     *
     * @param ex The exception
     * @return ApiResponse with error details
     */
    @ExceptionHandler(RentalNotFoundException.class)
    public ResponseEntity<?> handleRentalNotFoundException(RentalNotFoundException ex) {
        log.warn("Rental not found: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(HttpStatus.NOT_FOUND.value())
                .error("Not Found")
                .message(ex.getMessage())
                .timestamp(System.currentTimeMillis())
                .build();

        ApiResponse<?> response = ApiResponse.builder()
                .success(false)
                .message("Rental session not found")
                .data(errorResponse)
                .timestamp(System.currentTimeMillis())
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    /**
     * Handle LockAcquisitionException - Cannot acquire lock
     *
     * HTTP Status: 503 Service Unavailable (resource temporarily locked)
     * Includes Retry-After header suggesting client to retry after 5 seconds
     *
     * @param ex The exception
     * @return ApiResponse with error details and retry header
     */
    @ExceptionHandler(LockAcquisitionException.class)
    public ResponseEntity<?> handleLockAcquisitionException(LockAcquisitionException ex) {
        log.warn("Failed to acquire lock: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(HttpStatus.SERVICE_UNAVAILABLE.value())
                .error("Service Unavailable")
                .message(ex.getMessage())
                .timestamp(System.currentTimeMillis())
                .build();

        ApiResponse<?> response = ApiResponse.builder()
                .success(false)
                .message("Resource is temporarily locked, please retry")
                .data(errorResponse)
                .timestamp(System.currentTimeMillis())
                .build();

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .header("Retry-After", "5")  // Suggest retry after 5 seconds
                .body(response);
    }

    /**
     * Handle QueueFullException - Telemetry queue is full
     *
     * HTTP Status: 503 Service Unavailable (queue overloaded)
     * Includes Retry-After header suggesting client to retry after 10 seconds
     *
     * @param ex The exception
     * @return ApiResponse with error details and retry header
     */
    @ExceptionHandler(QueueFullException.class)
    public ResponseEntity<?> handleQueueFullException(QueueFullException ex) {
        log.warn("Queue is full: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(HttpStatus.SERVICE_UNAVAILABLE.value())
                .error("Service Unavailable")
                .message(ex.getMessage())
                .timestamp(System.currentTimeMillis())
                .build();

        ApiResponse<?> response = ApiResponse.builder()
                .success(false)
                .message("Service is temporarily overloaded, please retry")
                .data(errorResponse)
                .timestamp(System.currentTimeMillis())
                .build();

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .header("Retry-After", "10")  // Suggest retry after 10 seconds
                .body(response);
    }

    /**
     * Handle Validation Errors - @Valid annotation validation failures
     *
     * HTTP Status: 400 Bad Request
     * Includes field-level error messages
     *
     * @param ex The MethodArgumentNotValidException
     * @return ApiResponse with field errors
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        log.warn("Validation error occurred");

        // Extract field errors
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            fieldErrors.put(fieldName, errorMessage);
        });

        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Validation Failed")
                .message("Request validation failed")
                .timestamp(System.currentTimeMillis())
                .fieldErrors(fieldErrors)
                .build();

        ApiResponse<?> response = ApiResponse.builder()
                .success(false)
                .message("Request validation failed. Please check the field errors")
                .data(errorResponse)
                .timestamp(System.currentTimeMillis())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Handle 404 Not Found - Endpoint not found
     *
     * HTTP Status: 404 Not Found
     *
     * @param ex The NoHandlerFoundException
     * @return ApiResponse with error details
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<?> handleNoHandlerFoundException(NoHandlerFoundException ex) {
        log.warn("Endpoint not found: {} {}", ex.getHttpMethod(), ex.getRequestURL());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(HttpStatus.NOT_FOUND.value())
                .error("Not Found")
                .message("The requested endpoint does not exist")
                .timestamp(System.currentTimeMillis())
                .build();

        ApiResponse<?> response = ApiResponse.builder()
                .success(false)
                .message("Endpoint not found")
                .data(errorResponse)
                .timestamp(System.currentTimeMillis())
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    /**
     * Handle all other exceptions - Catch-all for unexpected errors
     *
     * HTTP Status: 500 Internal Server Error
     * Does NOT expose stack trace to client (logged server-side only)
     *
     * @param ex The generic Exception
     * @return ApiResponse with generic error message
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGenericException(Exception ex) {
        log.error("Unexpected error occurred", ex);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Internal Server Error")
                .message("An unexpected error occurred. Please contact support if the problem persists")
                .timestamp(System.currentTimeMillis())
                .build();

        ApiResponse<?> response = ApiResponse.builder()
                .success(false)
                .message("An unexpected error occurred")
                .data(errorResponse)
                .timestamp(System.currentTimeMillis())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
