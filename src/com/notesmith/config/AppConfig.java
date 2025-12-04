package com.notesmith.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.util.Properties;

/**
 * Application configuration loader.
 * Reads from user config (~/.notesmith/config.properties) first,
 * then bundled config.properties, or falls back to environment variables.
 */
public final class AppConfig {
    
    private static final Properties props = new Properties();
    
    static {
        // First, try to load user's config from home directory
        String userHome = System.getProperty("user.home");
        java.io.File userConfigFile = new java.io.File(userHome, ".notesmith/config.properties");
        
        if (userConfigFile.exists()) {
            try (FileInputStream input = new FileInputStream(userConfigFile)) {
                props.load(input);
            } catch (IOException e) {
                System.err.println("Warning: Could not load user config.properties");
            }
        }
        
        // Then, load bundled config (without overwriting user settings)
        try (InputStream input = AppConfig.class.getClassLoader()
                .getResourceAsStream("config.properties")) {
            if (input != null) {
                Properties bundledProps = new Properties();
                bundledProps.load(input);
                // Only add bundled properties that don't exist in user config
                for (String key : bundledProps.stringPropertyNames()) {
                    if (!props.containsKey(key)) {
                        props.setProperty(key, bundledProps.getProperty(key));
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Warning: Could not load bundled config.properties");
        }
    }
    
    private AppConfig() {}
    
    public static String getDbUrl() {
        // H2 embedded database - stored in user's home directory
        String userHome = System.getProperty("user.home");
        String dbPath = userHome + "/.notesmith/notesmith_db";
        return getProperty("db.url", "jdbc:h2:" + dbPath + ";AUTO_SERVER=TRUE");
    }
    
    public static String getDbUser() {
        return getProperty("db.user", "sa");
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
        return getProperty("ai.gemini.api.key", "");
    }
    
    public static String getGeminiModel() {
        return getProperty("ai.gemini.model", "gemini-2.5-flash-latest");
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
