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
            for (int i = 0; i < 3; i++) { // Start with 3 connections
                availableConnections.add(createConnection());
            }
            logger.info("Connection pool initialized with " + availableConnections.size() + " connections");
        } catch (SQLException e) {
            logger.error("Failed to initialize connection pool", e);
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
