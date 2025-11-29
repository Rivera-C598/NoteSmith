package com.notesmith.exception;

/**
 * Base exception for all NoteSmith application errors.
 */
public class NoteSmithException extends Exception {
    
    public NoteSmithException(String message) {
        super(message);
    }
    
    public NoteSmithException(String message, Throwable cause) {
        super(message, cause);
    }
}
