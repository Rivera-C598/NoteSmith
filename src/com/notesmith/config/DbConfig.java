package com.notesmith.config;

/**
 * @deprecated Use AppConfig instead for better security and flexibility
 */
@Deprecated
public class DbConfig {
    private DbConfig() {}

    public static final String JDBC_URL = AppConfig.getDbUrl();
    public static final String DB_USER = AppConfig.getDbUser();
    public static final String DB_PASSWORD = AppConfig.getDbPassword();
}
