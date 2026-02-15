package com.conor.taskmanager.exception;

import org.springframework.security.core.AuthenticationException;

/**
 * Exception thrown when login credentials are invalid.
 */
public class InvalidCredentialsException extends AuthenticationException {
    public InvalidCredentialsException(String message) {
        super(message);
    }
}
