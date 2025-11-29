package com.notesmith.model;

import java.time.LocalDateTime;

public class TextNote extends Note {

    public TextNote(String title, String content) {
        super(title, content, NoteType.TEXT);
    }

    public TextNote(String id, String title, String content,
                    LocalDateTime createdAt, LocalDateTime updatedAt) {
        super(id, title, content, createdAt, updatedAt, NoteType.TEXT);
    }

    @Override
    public String display() {
        return "[TEXT] " + getTitle() + " - " + getContent();
    }
}
