package com.notesmith.model;

import java.time.LocalDateTime;

public class TodoNote extends Note {

    private boolean done;

    public TodoNote(String title, String content, boolean done) {
        super(title, content, NoteType.TODO);
        this.done = done;
    }

    public TodoNote(String id, String title, String content,
                    LocalDateTime createdAt, LocalDateTime updatedAt,
                    boolean done) {
        super(id, title, content, createdAt, updatedAt, NoteType.TODO);
        this.done = done;
    }

    public boolean isDone() { return done; }

    public void setDone(boolean done) {
        this.done = done;
    }

    @Override
    public String display() {
        return (done ? "[DONE] " : "[TODO] ") + getTitle() + " - " + getContent();
    }
}
