package com.notesmith.exception;

/**
 * Exception thrown for database and file persistence errors.
 */
public class PersistenceException extends NoteSmithException {
    
    public PersistenceException(String message) {
        super(message);
    }
    
    public PersistenceException(String message, Throwable cause) {
        super(message, cause);
    }
}
