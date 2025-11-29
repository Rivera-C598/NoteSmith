package com.notesmith.model;

import java.time.LocalDateTime;
import java.util.UUID;

public abstract class Note {
    private final String id;             // UUID
    private String title;
    private String content;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private final NoteType type;

    protected Note(String title, String content, NoteType type) {
        this.id = UUID.randomUUID().toString();
        this.title = title;
        this.content = content;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
        this.type = type;
    }

    protected Note(String id, String title, String content,
                   LocalDateTime createdAt, LocalDateTime updatedAt,
                   NoteType type) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.type = type;
    }

    public String getId() { return id; }

    public String getTitle() { return title; }

    public void setTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Title cannot be empty.");
        }
        this.title = title;
        this.updatedAt = LocalDateTime.now();
    }

    public String getContent() { return content; }

    public void setContent(String content) {
        this.content = content;
        this.updatedAt = LocalDateTime.now();
    }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public NoteType getType() { return type; }

    // Polymorphic behavior
    public abstract String display();
}
