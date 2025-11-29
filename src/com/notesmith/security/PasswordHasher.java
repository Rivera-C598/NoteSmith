package com.notesmith.security;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Password hashing using bcrypt.
 * This is much safer for password storage than plain SHA algorithms.
 *
 * bcrypt automatically:
 * - generates a random salt
 * - embeds the salt and cost factor into the hash string
 */
public final class PasswordHasher {

    // Work factor (cost). 10-12 is normal for dev machines.
    // Higher = slower = more resistant to brute force.
    private static final int COST = 12;

    private PasswordHasher() {
        // utility class, no instances
    }

    /**
     * Hash a plaintext password using bcrypt.
     * The returned string contains algorithm, cost, salt, and hash.
     */
    public static String hash(String plainPassword) {
        if (plainPassword == null) {
            throw new IllegalArgumentException("Password cannot be null.");
        }
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(COST));
    }

    /**
     * Verify a plaintext password against a stored bcrypt hash.
     */
    public static boolean verify(String plainPassword, String hash) {
        if (plainPassword == null || hash == null || hash.isEmpty()) {
            return false;
        }
        try {
            return BCrypt.checkpw(plainPassword, hash);
        } catch (IllegalArgumentException ex) {
            // If the hash string is malformed
            return false;
        }
    }
}
