package com.smartmove.exception;

/**
 * @author jniyi
 * @project smartmove-core-engine - 2026
 * @created 15.02.2026
 */

public class SmartMoveException extends RuntimeException {
    public SmartMoveException(String message) {
        super(message);
    }

    public SmartMoveException(String message, Throwable cause) {
        super(message, cause);
    }
}
