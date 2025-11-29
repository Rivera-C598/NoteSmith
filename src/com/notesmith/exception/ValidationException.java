package com.notesmith.exception;

/**
 * Exception thrown for validation errors.
 */
public class ValidationException extends NoteSmithException {
    
    public ValidationException(String message) {
        super(message);
    }
}
