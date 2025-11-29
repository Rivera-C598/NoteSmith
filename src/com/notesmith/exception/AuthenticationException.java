package com.notesmith.exception;

/**
 * Exception thrown for authentication-related errors.
 */
public class AuthenticationException extends NoteSmithException {
    
    public AuthenticationException(String message) {
        super(message);
    }
    
    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
