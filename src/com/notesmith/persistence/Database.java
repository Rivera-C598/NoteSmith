package com.notesmith.persistence;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Database connection manager using connection pooling.
 */
public class Database {

    public static Connection getConnection() throws SQLException {
        return ConnectionPool.getInstance().getConnection();
    }
    
    public static void releaseConnection(Connection connection) {
        ConnectionPool.getInstance().releaseConnection(connection);
    }
    
    public static void shutdown() {
        ConnectionPool.getInstance().shutdown();
    }
}
