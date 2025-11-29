package com.notesmith.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Application configuration loader.
 * Reads from config.properties or falls back to environment variables.
 */
public final class AppConfig {
    
    private static final Properties props = new Properties();
    
    static {
        try (InputStream input = AppConfig.class.getClassLoader()
                .getResourceAsStream("config.properties")) {
            if (input != null) {
                props.load(input);
            }
        } catch (IOException e) {
            System.err.println("Warning: Could not load config.properties, using defaults");
        }
    }
    
    private AppConfig() {}
    
    public static String getDbUrl() {
        return getProperty("db.url", "jdbc:mysql://localhost:3306/notesmith_db?useSSL=false&serverTimezone=UTC");
    }
    
    public static String getDbUser() {
        return getProperty("db.user", "root");
    }
    
    public static String getDbPassword() {
        return getProperty("db.password", "");
    }
    
    public static int getDbPoolSize() {
        return Integer.parseInt(getProperty("db.pool.size", "10"));
    }
    
    public static int getMaxLoginAttempts() {
        return Integer.parseInt(getProperty("security.max.login.attempts", "5"));
    }
    
    public static int getPasswordMinLength() {
        return Integer.parseInt(getProperty("security.password.min.length", "8"));
    }
    
    // AI Configuration
    public static String getGeminiApiKey() {
        return getProperty("ai.gemini.api.key", "AIzaSyCS6inBC1rdC2hB24f9cMpyQhzsvafhN6A");
    }
    
    public static String getGeminiModel() {
        return getProperty("ai.gemini.model", "gemini-2.5-flash");
    }
    
    public static boolean isAIEnabled() {
        return Boolean.parseBoolean(getProperty("ai.enabled", "true"));
    }
    
    public static boolean isAICacheEnabled() {
        return Boolean.parseBoolean(getProperty("ai.cache.enabled", "true"));
    }
    
    public static int getAIMaxTokens() {
        return Integer.parseInt(getProperty("ai.max.tokens", "1000"));
    }
    
    private static String getProperty(String key, String defaultValue) {
        // Check environment variable first (uppercase with underscores)
        String envKey = key.toUpperCase().replace('.', '_');
        String envValue = System.getenv(envKey);
        if (envValue != null && !envValue.isEmpty()) {
            return envValue;
        }
        // Fall back to properties file
        return props.getProperty(key, defaultValue);
    }
}
