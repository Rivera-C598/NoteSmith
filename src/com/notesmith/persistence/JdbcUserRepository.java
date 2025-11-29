package com.notesmith.persistence;

import com.notesmith.exception.PersistenceException;
import com.notesmith.model.User;
import com.notesmith.util.Logger;

import java.sql.*;

public class JdbcUserRepository implements UserRepository {
    
    private static final Logger logger = Logger.getLogger(JdbcUserRepository.class);

    @Override
    public User findByUsername(String username) throws PersistenceException {
        String sql = "SELECT id, username, password_hash FROM users WHERE username = ?";
        Connection conn = null;

        try {
            conn = Database.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, username);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return new User(
                                rs.getInt("id"),
                                rs.getString("username"),
                                rs.getString("password_hash")
                        );
                    }
                    return null;
                }
            }
        } catch (SQLException e) {
            logger.error("Failed to find user: " + username, e);
            throw new PersistenceException("Failed to retrieve user from database", e);
        } finally {
            if (conn != null) {
                Database.releaseConnection(conn);
            }
        }
    }

    @Override
    public void createUser(String username, String passwordHash) throws PersistenceException {
        String sql = "INSERT INTO users(username, password_hash) VALUES(?, ?)";
        Connection conn = null;

        try {
            conn = Database.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, username);
                ps.setString(2, passwordHash);
                ps.executeUpdate();
                logger.info("User created: " + username);
            }
        } catch (SQLException e) {
            logger.error("Failed to create user: " + username, e);
            throw new PersistenceException("Failed to create user in database", e);
        } finally {
            if (conn != null) {
                Database.releaseConnection(conn);
            }
        }
    }
}
