package com.conor.taskmanager.exception;

/**
 * Exception thrown when a user lacks permission to perform an action.
 * Returns 403 FORBIDDEN status.
 */
public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
    }
}
