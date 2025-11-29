package com.notesmith.persistence;

import com.notesmith.exception.PersistenceException;
import com.notesmith.model.Note;
import com.notesmith.util.Logger;

import java.util.List;

/**
 * NoteRepository that writes to BOTH DB and file.
 * - Reads from DB (fallback to file if DB fails)
 * - Saves/deletes in DB and then in file
 */
public class DualNoteRepository implements NoteRepository {
    
    private static final Logger logger = Logger.getLogger(DualNoteRepository.class);
    private final NoteRepository fileRepo;
    private final NoteRepository dbRepo;

    public DualNoteRepository(NoteRepository fileRepo, NoteRepository dbRepo) {
        this.fileRepo = fileRepo;
        this.dbRepo = dbRepo;
    }

    @Override
    public List<Note> findAll() throws PersistenceException {
        try {
            return dbRepo.findAll();
        } catch (PersistenceException e) {
            logger.warn("Database read failed, falling back to file");
            return fileRepo.findAll();
        }
    }

    @Override
    public void save(Note note) throws PersistenceException {
        PersistenceException dbException = null;
        PersistenceException fileException = null;
        
        try {
            dbRepo.save(note);
        } catch (PersistenceException e) {
            logger.error("Failed to save to database", e);
            dbException = e;
        }
        
        try {
            fileRepo.save(note);
        } catch (PersistenceException e) {
            logger.error("Failed to save to file", e);
            fileException = e;
        }
        
        // If both failed, throw exception
        if (dbException != null && fileException != null) {
            throw new PersistenceException("Failed to save note to both database and file", dbException);
        }
    }

    @Override
    public void delete(String id) throws PersistenceException {
        PersistenceException dbException = null;
        PersistenceException fileException = null;
        
        try {
            dbRepo.delete(id);
        } catch (PersistenceException e) {
            logger.error("Failed to delete from database", e);
            dbException = e;
        }
        
        try {
            fileRepo.delete(id);
        } catch (PersistenceException e) {
            logger.error("Failed to delete from file", e);
            fileException = e;
        }
        
        // If both failed, throw exception
        if (dbException != null && fileException != null) {
            throw new PersistenceException("Failed to delete note from both database and file", dbException);
        }
    }
}
