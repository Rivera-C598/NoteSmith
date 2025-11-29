package com.notesmith.persistence;

import com.notesmith.exception.PersistenceException;
import com.notesmith.model.Note;
import com.notesmith.model.NoteType;
import com.notesmith.model.TextNote;
import com.notesmith.model.TodoNote;
import com.notesmith.util.Logger;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class JdbcNoteRepository implements NoteRepository {
    
    private static final Logger logger = Logger.getLogger(JdbcNoteRepository.class);
    private final int userId;

    public JdbcNoteRepository(int userId) {
        this.userId = userId;
    }

    @Override
    public List<Note> findAll() throws PersistenceException {
        List<Note> notes = new ArrayList<>();

        String sql = "SELECT id, title, content, created_at, updated_at, type, done, tags, pinned " +
                "FROM notes WHERE user_id = ? ORDER BY pinned DESC, created_at DESC";
        
        Connection conn = null;
        try {
            conn = Database.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, userId);

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String id = rs.getString("id");
                        String title = rs.getString("title");
                        String content = rs.getString("content");
                        LocalDateTime createdAt = rs.getTimestamp("created_at").toLocalDateTime();
                        LocalDateTime updatedAt = rs.getTimestamp("updated_at").toLocalDateTime();
                        NoteType type = NoteType.valueOf(rs.getString("type"));
                        boolean done = rs.getBoolean("done");
                        String tagsStr = rs.getString("tags");
                        boolean pinned = rs.getBoolean("pinned");

                        Note note;
                        if (type == NoteType.TODO) {
                            note = new TodoNote(id, title, content, createdAt, updatedAt, done);
                        } else {
                            note = new TextNote(id, title, content, createdAt, updatedAt);
                        }
                        
                        // Parse tags
                        if (tagsStr != null && !tagsStr.isEmpty()) {
                            String[] tagArray = tagsStr.split(",");
                            for (String tag : tagArray) {
                                note.addTag(tag.trim());
                            }
                        }
                        
                        note.setPinned(pinned);
                        notes.add(note);
                    }
                }
            }
        } catch (SQLException e) {
            throw new PersistenceException("Database error loading notes", e);
        } finally {
            if (conn != null) {
                Database.releaseConnection(conn);
            }
        }

        return notes;
    }

    @Override
    public void save(Note note) throws PersistenceException {
        String sqlUpdate =
                "UPDATE notes SET title = ?, content = ?, updated_at = ?, type = ?, done = ?, tags = ?, pinned = ? " +
                        "WHERE id = ? AND user_id = ?";
        String sqlInsert =
                "INSERT INTO notes (id, user_id, title, content, created_at, updated_at, type, done, tags, pinned) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        LocalDateTime createdAt = note.getCreatedAt();
        LocalDateTime updatedAt = note.getUpdatedAt();
        boolean done = (note instanceof TodoNote) && ((TodoNote) note).isDone();
        String tags = String.join(",", note.getTags());
        boolean pinned = note.isPinned();

        Connection conn = null;
        try {
            conn = Database.getConnection();
            // Try update first
            try (PreparedStatement ps = conn.prepareStatement(sqlUpdate)) {
                ps.setString(1, note.getTitle());
                ps.setString(2, note.getContent());
                ps.setTimestamp(3, Timestamp.valueOf(updatedAt));
                ps.setString(4, note.getType().name());
                ps.setBoolean(5, done);
                ps.setString(6, tags);
                ps.setBoolean(7, pinned);
                ps.setString(8, note.getId());
                ps.setInt(9, userId);

                int rows = ps.executeUpdate();
                if (rows == 0) {
                    // Insert if no row updated
                    try (PreparedStatement psIns = conn.prepareStatement(sqlInsert)) {
                        psIns.setString(1, note.getId());
                        psIns.setInt(2, userId);
                        psIns.setString(3, note.getTitle());
                        psIns.setString(4, note.getContent());
                        psIns.setTimestamp(5, Timestamp.valueOf(createdAt));
                        psIns.setTimestamp(6, Timestamp.valueOf(updatedAt));
                        psIns.setString(7, note.getType().name());
                        psIns.setBoolean(8, done);
                        psIns.setString(9, tags);
                        psIns.setBoolean(10, pinned);
                        psIns.executeUpdate();
                    }
                }
            }
            logger.info("Note saved: " + note.getId());
        } catch (SQLException e) {
            logger.error("Failed to save note: " + note.getId(), e);
            throw new PersistenceException("Database error saving note", e);
        } finally {
            if (conn != null) {
                Database.releaseConnection(conn);
            }
        }
    }

    @Override
    public void delete(String id) throws PersistenceException {
        String sql = "DELETE FROM notes WHERE id = ? AND user_id = ?";
        
        Connection conn = null;
        try {
            conn = Database.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, id);
                ps.setInt(2, userId);
                ps.executeUpdate();
            }
            logger.info("Note deleted: " + id);
        } catch (SQLException e) {
            logger.error("Failed to delete note: " + id, e);
            throw new PersistenceException("Database error deleting note", e);
        } finally {
            if (conn != null) {
                Database.releaseConnection(conn);
            }
        }
    }
}
