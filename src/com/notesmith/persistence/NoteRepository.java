package com.notesmith.persistence;

import com.notesmith.exception.PersistenceException;
import com.notesmith.model.Note;

import java.util.List;

public interface NoteRepository {
    List<Note> findAll() throws PersistenceException;
    void save(Note note) throws PersistenceException;
    void delete(String id) throws PersistenceException;
}
