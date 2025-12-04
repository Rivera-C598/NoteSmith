package com.notesmith.persistence;

import com.notesmith.config.AppConfig;
import com.notesmith.util.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Simple connection pool implementation.
 * In production, use HikariCP or Apache DBCP.
 */
public class ConnectionPool {
    
    private static final Logger logger = Logger.getLogger(ConnectionPool.class);
    private static ConnectionPool instance;
    
    private final List<Connection> availableConnections = new ArrayList<>();
    private final List<Connection> usedConnections = new ArrayList<>();
    private final int maxPoolSize;
    
    private ConnectionPool() {
        this.maxPoolSize = AppConfig.getDbPoolSize();
        
        // Load H2 database driver
        try {
            Class.forName("org.h2.Driver");
            logger.info("H2 database driver loaded successfully");
        } catch (ClassNotFoundException e) {
            logger.error("H2 database driver not found!", e);
            System.err.println("CRITICAL ERROR: H2 database driver not found!");
            System.err.println("Make sure h2-*.jar is in the lib/ folder");
            e.printStackTrace();
            throw new RuntimeException("Failed to load H2 database driver", e);
        }
        
        logger.info("Initializing database connection pool...");
        logger.info("Database URL: " + AppConfig.getDbUrl());
        
        initializePool();
    }
    
    public static synchronized ConnectionPool getInstance() {
        if (instance == null) {
            instance = new ConnectionPool();
        }
        return instance;
    }
    
    private void initializePool() {
        try {
            // Initialize H2 database schema
            initializeSchema();
            
            for (int i = 0; i < 3; i++) { // Start with 3 connections
                availableConnections.add(createConnection());
            }
            logger.info("Connection pool initialized with " + availableConnections.size() + " connections");
        } catch (SQLException e) {
            logger.error("Failed to initialize connection pool", e);
        }
    }
    
    private void initializeSchema() throws SQLException {
        logger.info("Initializing database schema...");
        try (Connection conn = createConnection();
             var statement = conn.createStatement()) {
            
            // Read and execute schema.sql
            String schema = """
                CREATE TABLE IF NOT EXISTS users (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    username VARCHAR(50) UNIQUE NOT NULL,
                    password_hash VARCHAR(255) NOT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                );
                
                CREATE TABLE IF NOT EXISTS notes (
                    id VARCHAR(36) PRIMARY KEY,
                    user_id INT NOT NULL,
                    title VARCHAR(200) NOT NULL,
                    content TEXT,
                    created_at TIMESTAMP NOT NULL,
                    updated_at TIMESTAMP NOT NULL,
                    type VARCHAR(20) NOT NULL,
                    done BOOLEAN DEFAULT FALSE,
                    tags VARCHAR(500) DEFAULT '',
                    pinned BOOLEAN DEFAULT FALSE,
                    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
                );
                
                CREATE INDEX IF NOT EXISTS idx_user_pinned_created ON notes(user_id, pinned DESC, created_at DESC);
                """;
            
            statement.execute(schema);
            logger.info("Database schema initialized successfully");
        } catch (SQLException e) {
            logger.error("Failed to initialize database schema", e);
            System.err.println("CRITICAL ERROR: Failed to initialize database schema");
            e.printStackTrace();
            throw e;
        }
    }
    
    private Connection createConnection() throws SQLException {
        return DriverManager.getConnection(
            AppConfig.getDbUrl(),
            AppConfig.getDbUser(),
            AppConfig.getDbPassword()
        );
    }
    
    public synchronized Connection getConnection() throws SQLException {
        if (availableConnections.isEmpty()) {
            if (usedConnections.size() < maxPoolSize) {
                availableConnections.add(createConnection());
            } else {
                throw new SQLException("Connection pool exhausted. Max pool size: " + maxPoolSize);
            }
        }
        
        Connection connection = availableConnections.remove(availableConnections.size() - 1);
        
        // Check if connection is still valid
        if (!connection.isValid(2)) {
            connection = createConnection();
        }
        
        usedConnections.add(connection);
        return connection;
    }
    
    public synchronized void releaseConnection(Connection connection) {
        if (connection != null) {
            usedConnections.remove(connection);
            availableConnections.add(connection);
        }
    }
    
    public synchronized void shutdown() {
        logger.info("Shutting down connection pool");
        closeConnections(usedConnections);
        closeConnections(availableConnections);
    }
    
    private void closeConnections(List<Connection> connections) {
        for (Connection conn : connections) {
            try {
                if (conn != null && !conn.isClosed()) {
                    conn.close();
                }
            } catch (SQLException e) {
                logger.error("Error closing connection", e);
            }
        }
        connections.clear();
    }
}
