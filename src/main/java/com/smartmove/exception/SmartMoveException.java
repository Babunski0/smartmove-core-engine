package com.smartmove.exception;

public class SmartMoveException extends RuntimeException {

    public SmartMoveException(String message) {
        super(message);
    }

    public SmartMoveException(String message, Throwable cause) {
        super(message, cause);
    }
}
