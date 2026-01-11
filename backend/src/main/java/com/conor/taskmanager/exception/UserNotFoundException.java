package com.conor.taskmanager.exception;

/**
 * Exception thrown when a user is not found in the database.
 * Returns 404 NOT_FOUND status.
 */
public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String message) {
        super(message);
    }
}
