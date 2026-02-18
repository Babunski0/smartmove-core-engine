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


import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // Error type constants
    private static final String BAD_REQUEST = "Bad Request";
    private static final String CONFLICT = "Conflict";
    private static final String INVALID_ARGUMENT = "Invalid Argument";
    private static final String NULL_POINTER_ERROR = "Null Pointer Error";
    private static final String INTERNAL_SERVER_ERROR = "Internal Server Error";
    private static final String IO_ERROR = "IO Error";
    private static final String VALIDATION_FAILED = "Validation Failed";
    private static final String SERVICE_UNAVAILABLE = "Service Unavailable";

    // Message constants
    private static final String VEHICLE_NOT_AVAILABLE_MSG = "Vehicle is not available for this operation";
    private static final String VALIDATION_FAILED_MSG = "Request validation failed. Please check the field errors";
    private static final String VALIDATION_REQUEST_MSG = "Request validation failed";
    private static final String INVALID_ARGUMENT_MSG = "Invalid argument provided";
    private static final String INTERNAL_SERVER_ERROR_MSG = "An unexpected error occurred. Please contact support if the problem persists";
    private static final String LOCK_TIMEOUT_MSG = "Resource is temporarily locked, please retry";
    private static final String IO_ERROR_MSG = "An error occurred while reading/writing data";
    private static final String IO_ERROR_RESPONSE_MSG = "IO error occurred";
    private static final String GENERIC_ERROR_RESPONSE_MSG = "An unexpected error occurred";
    private static final String INTERNAL_SERVER_ERROR_RESPONSE_MSG = "Internal server error";
    private static final String UNEXPECTED_ERROR_MSG = "An unexpected error occurred while processing your request";

    // Header constants
    private static final String RETRY_AFTER_HEADER = "Retry-After";
    private static final String RETRY_AFTER_SECONDS = "5";

    /**
     * Helper method to build ErrorResponse
     * Eliminates code duplication across exception handlers
     *
     * @param status HTTP status code
     * @param error Error type/category
     * @param message Error message
     * @return ErrorResponse object
     */
    private ErrorResponse buildErrorResponse(int status, String error, String message) {
        return ErrorResponse.builder()
                .status(status)
                .error(error)
                .message(message)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * Helper method to build ErrorResponse with field errors
     *
     * @param status HTTP status code
     * @param error Error type/category
     * @param message Error message
     * @param fieldErrors Map of field validation errors
     * @return ErrorResponse object
     */
    private ErrorResponse buildErrorResponseWithFieldErrors(int status, String error, String message, Map<String, String> fieldErrors) {
        return ErrorResponse.builder()
                .status(status)
                .error(error)
                .message(message)
                .fieldErrors(fieldErrors)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * Helper method to build ApiResponse with error data
     * Eliminates code duplication across exception handlers
     *
     * @param errorResponse The error response object
     * @param message User-facing message
     * @return ApiResponse containing the error response
     */
    private ApiResponse<ErrorResponse> buildErrorApiResponse(ErrorResponse errorResponse, String message) {
        return ApiResponse.<ErrorResponse>builder()
                .success(false)
                .message(message)
                .data(errorResponse)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * Handle SmartMoveException (Custom Application Exception)
     * Returns 400 BAD_REQUEST status
     *
     * @param ex SmartMoveException thrown
     * @return ResponseEntity with error details and 400 status
     */
    @ExceptionHandler(SmartMoveException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleSmartMoveException(SmartMoveException ex) {
        log.error("SmartMoveException occurred: {}", ex.getMessage(), ex);

        ErrorResponse errorResponse = buildErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                BAD_REQUEST,
                ex.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(buildErrorApiResponse(errorResponse, ex.getMessage()));
    }

    /**
     * Handle VehicleNotAvailableException
     * Returns 409 CONFLICT status
     *
     * @param ex VehicleNotAvailableException thrown
     * @return ResponseEntity with error details and 409 status
     */
    @ExceptionHandler(VehicleNotAvailableException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleVehicleNotAvailableException(
            VehicleNotAvailableException ex) {
        log.error("VehicleNotAvailableException occurred: {}", ex.getMessage(), ex);

        ErrorResponse errorResponse = buildErrorResponse(
                HttpStatus.CONFLICT.value(),
                CONFLICT,
                ex.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(buildErrorApiResponse(errorResponse, VEHICLE_NOT_AVAILABLE_MSG));
    }

    /**
     * Handle LockAcquisitionException
     * Returns 503 SERVICE_UNAVAILABLE status with Retry-After header
     *
     * @param ex LockAcquisitionException thrown
     * @return ResponseEntity with error details and 503 status
     */
    @ExceptionHandler(LockAcquisitionException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleLockAcquisitionException(
            LockAcquisitionException ex) {
        log.error("LockAcquisitionException occurred: {}", ex.getMessage(), ex);

        ErrorResponse errorResponse = buildErrorResponse(
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                SERVICE_UNAVAILABLE,
                ex.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .header(RETRY_AFTER_HEADER, RETRY_AFTER_SECONDS)
                .body(buildErrorApiResponse(errorResponse, LOCK_TIMEOUT_MSG));
    }

    /**
     * Handle Validation Errors from @Valid annotation
     * Returns 400 BAD_REQUEST status with detailed field errors
     *
     * @param ex MethodArgumentNotValidException thrown
     * @return ResponseEntity with field validation errors and 400 status
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex) {
        log.warn("Validation error occurred: {}", ex.getMessage());

        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            fieldErrors.put(fieldName, errorMessage);
        });

        ErrorResponse errorResponse = buildErrorResponseWithFieldErrors(
                HttpStatus.BAD_REQUEST.value(),
                VALIDATION_FAILED,
                VALIDATION_REQUEST_MSG,
                fieldErrors
        );

        ApiResponse<ErrorResponse> response = ApiResponse.<ErrorResponse>builder()
                .success(false)
                .message(VALIDATION_FAILED_MSG)
                .data(errorResponse)
                .timestamp(System.currentTimeMillis())
                .build();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    /**
     * Handle IllegalArgumentException
     * Returns 400 BAD_REQUEST status
     *
     * @param ex IllegalArgumentException thrown
     * @return ResponseEntity with error details and 400 status
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleIllegalArgumentException(
            IllegalArgumentException ex) {
        log.error("IllegalArgumentException occurred: {}", ex.getMessage(), ex);

        ErrorResponse errorResponse = buildErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                INVALID_ARGUMENT,
                ex.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(buildErrorApiResponse(errorResponse, INVALID_ARGUMENT_MSG));
    }

    /**
     * Handle NullPointerException
     * Returns 500 INTERNAL_SERVER_ERROR status
     *
     * @param ex NullPointerException thrown
     * @return ResponseEntity with error details and 500 status
     */
    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleNullPointerException(NullPointerException ex) {
        log.error("NullPointerException occurred: {}", ex.getMessage(), ex);

        ErrorResponse errorResponse = buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                NULL_POINTER_ERROR,
                UNEXPECTED_ERROR_MSG
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(buildErrorApiResponse(errorResponse, INTERNAL_SERVER_ERROR_RESPONSE_MSG));
    }

    /**
     * Handle IOException (file read/write errors)
     * Returns 500 INTERNAL_SERVER_ERROR status
     *
     * @param ex IOException thrown
     * @return ResponseEntity with error details and 500 status
     */
    @ExceptionHandler(IOException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleIOException(IOException ex) {
        log.error("IOException occurred: {}", ex.getMessage(), ex);

        ErrorResponse errorResponse = buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                IO_ERROR,
                IO_ERROR_MSG
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(buildErrorApiResponse(errorResponse, IO_ERROR_RESPONSE_MSG));
    }

    /**
     * Handle all other generic Exceptions (catch-all handler)
     * Returns 500 INTERNAL_SERVER_ERROR status
     * This should be the last handler as it catches all remaining exceptions
     *
     * @param ex Exception thrown
     * @return ResponseEntity with error details and 500 status
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleGenericException(Exception ex) {
        log.error("Unexpected exception occurred: {}", ex.getMessage(), ex);

        ErrorResponse errorResponse = buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                INTERNAL_SERVER_ERROR,
                INTERNAL_SERVER_ERROR_MSG
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(buildErrorApiResponse(errorResponse, GENERIC_ERROR_RESPONSE_MSG));
    }
}
