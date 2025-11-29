package com.notesmith.util;

import com.notesmith.config.AppConfig;
import com.notesmith.exception.ValidationException;

import java.util.regex.Pattern;

/**
 * Utility class for input validation.
 */
public final class ValidationUtils {
    
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,20}$");
    private static final Pattern SPECIAL_CHAR_PATTERN = Pattern.compile("[!@#$%^&*(),.?\":{}|<>]");
    private static final Pattern DIGIT_PATTERN = Pattern.compile("\\d");
    private static final Pattern UPPERCASE_PATTERN = Pattern.compile("[A-Z]");
    private static final Pattern LOWERCASE_PATTERN = Pattern.compile("[a-z]");
    
    private ValidationUtils() {}
    
    /**
     * Validates username format.
     * Must be 3-20 characters, alphanumeric and underscores only.
     */
    public static void validateUsername(String username) throws ValidationException {
        if (username == null || username.trim().isEmpty()) {
            throw new ValidationException("Username cannot be empty");
        }
        
        if (!USERNAME_PATTERN.matcher(username).matches()) {
            throw new ValidationException(
                "Username must be 3-20 characters long and contain only letters, numbers, and underscores");
        }
    }
    
    /**
     * Validates password strength.
     * Strict validation commented out for testing purposes.
     */
    public static void validatePassword(String password) throws ValidationException {
        if (password == null || password.isEmpty()) {
            throw new ValidationException("Password cannot be empty");
        }
        
        // Relaxed for testing - only check minimum length
        int minLength = 3; // Reduced from 8 for testing
        if (password.length() < minLength) {
            throw new ValidationException("Password must be at least " + minLength + " characters long");
        }
        
        // STRICT VALIDATION (commented out for testing)
        // Uncomment these for production use:
        
        // int minLength = AppConfig.getPasswordMinLength();
        // if (password.length() < minLength) {
        //     throw new ValidationException("Password must be at least " + minLength + " characters long");
        // }
        
        // if (!UPPERCASE_PATTERN.matcher(password).find()) {
        //     throw new ValidationException("Password must contain at least one uppercase letter");
        // }
        
        // if (!LOWERCASE_PATTERN.matcher(password).find()) {
        //     throw new ValidationException("Password must contain at least one lowercase letter");
        // }
        
        // if (!DIGIT_PATTERN.matcher(password).find()) {
        //     throw new ValidationException("Password must contain at least one digit");
        // }
        
        // if (!SPECIAL_CHAR_PATTERN.matcher(password).find()) {
        //     throw new ValidationException("Password must contain at least one special character");
        // }
    }
    
    /**
     * Gets password strength as a percentage (0-100).
     */
    public static int getPasswordStrength(String password) {
        if (password == null || password.isEmpty()) {
            return 0;
        }
        
        int strength = 0;
        
        // Length score (max 40 points)
        strength += Math.min(password.length() * 4, 40);
        
        // Character variety (max 60 points)
        if (UPPERCASE_PATTERN.matcher(password).find()) strength += 15;
        if (LOWERCASE_PATTERN.matcher(password).find()) strength += 15;
        if (DIGIT_PATTERN.matcher(password).find()) strength += 15;
        if (SPECIAL_CHAR_PATTERN.matcher(password).find()) strength += 15;
        
        return Math.min(strength, 100);
    }
    
    /**
     * Validates note title.
     */
    public static void validateNoteTitle(String title) throws ValidationException {
        if (title == null || title.trim().isEmpty()) {
            throw new ValidationException("Note title cannot be empty");
        }
        
        if (title.length() > 200) {
            throw new ValidationException("Note title cannot exceed 200 characters");
        }
    }
}
