package com.notesmith.persistence;

import com.notesmith.exception.PersistenceException;
import com.notesmith.model.*;
import com.notesmith.util.Logger;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class FileNoteRepository implements NoteRepository {
    
    private static final Logger logger = Logger.getLogger(FileNoteRepository.class);
    private final Path filePath;

    public FileNoteRepository(String filename) {
        this.filePath = Paths.get(filename);
    }

    @Override
    public List<Note> findAll() throws PersistenceException {
        List<Note> notes = new ArrayList<>();

        if (!Files.exists(filePath)) {
            return notes;
        }

        try (BufferedReader reader = Files.newBufferedReader(filePath)) {
            String line;
            while ((line = reader.readLine()) != null) {
                Note note = parseLine(line);
                if (note != null) {
                    notes.add(note);
                }
            }
        } catch (IOException e) {
            logger.error("Failed to read notes from file: " + filePath, e);
            throw new PersistenceException("Failed to read notes from file", e);
        }

        return notes;
    }

    @Override
    public void save(Note note) throws PersistenceException {
        List<Note> existing = findAll();
        boolean updated = false;

        for (int i = 0; i < existing.size(); i++) {
            if (existing.get(i).getId().equals(note.getId())) {
                existing.set(i, note);
                updated = true;
                break;
            }
        }

        if (!updated) {
            existing.add(note);
        }

        try (BufferedWriter writer = Files.newBufferedWriter(filePath)) {
            for (Note n : existing) {
                writer.write(formatLine(n));
                writer.newLine();
            }
            logger.info("Note saved to file: " + note.getId());
        } catch (IOException e) {
            logger.error("Failed to save note to file: " + note.getId(), e);
            throw new PersistenceException("Failed to save note to file", e);
        }
    }

    @Override
    public void delete(String id) throws PersistenceException {
        List<Note> existing = findAll();
        existing.removeIf(n -> n.getId().equals(id));

        try (BufferedWriter writer = Files.newBufferedWriter(filePath)) {
            for (Note n : existing) {
                writer.write(formatLine(n));
                writer.newLine();
            }
            logger.info("Note deleted from file: " + id);
        } catch (IOException e) {
            logger.error("Failed to delete note from file: " + id, e);
            throw new PersistenceException("Failed to delete note from file", e);
        }
    }

    private String formatLine(Note n) {
        String safeTitle = n.getTitle().replace("|", "\\|");
        String safeContent = n.getContent().replace("|", "\\|");

        String created = n.getCreatedAt().toString();
        String updated = n.getUpdatedAt().toString();

        String type = n.getType().name();
        String doneValue = "";

        if (n instanceof TodoNote) {
            doneValue = String.valueOf(((TodoNote) n).isDone());
        }

        return String.join("|",
                n.getId(),
                type,
                safeTitle,
                safeContent,
                created,
                updated,
                doneValue
        );
    }

    private Note parseLine(String line) {
        String[] parts = line.split("\\|", 7);
        if (parts.length < 6) return null;

        String id = parts[0];
        NoteType type = NoteType.valueOf(parts[1]);
        String title = parts[2].replace("\\|", "|");
        String content = parts[3].replace("\\|", "|");
        LocalDateTime created = LocalDateTime.parse(parts[4]);
        LocalDateTime updated = LocalDateTime.parse(parts[5]);

        switch (type) {
            case TEXT:
                return new TextNote(id, title, content, created, updated);
            case TODO:
                boolean done = parts.length > 6 && Boolean.parseBoolean(parts[6]);
                return new TodoNote(id, title, content, created, updated, done);
            default:
                return null;
        }
    }
}
