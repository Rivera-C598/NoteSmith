package com.notesmith.util;

import com.notesmith.model.Note;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Utility class for exporting notes to various formats.
 */
public final class ExportUtils {
    
    private static final Logger logger = Logger.getLogger(ExportUtils.class);
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    
    private ExportUtils() {}
    
    /**
     * Export a single note to Markdown format.
     */
    public static void exportToMarkdown(Note note, String filePath) throws IOException {
        Path path = Paths.get(filePath);
        
        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
            // Title
            writer.write("# " + note.getTitle());
            writer.newLine();
            writer.newLine();
            
            // Metadata
            writer.write("**Created:** " + note.getCreatedAt().format(formatter));
            writer.newLine();
            writer.write("**Updated:** " + note.getUpdatedAt().format(formatter));
            writer.newLine();
            
            // Tags
            if (!note.getTags().isEmpty()) {
                writer.write("**Tags:** " + String.join(", ", note.getTags()));
                writer.newLine();
            }
            
            // Pinned status
            if (note.isPinned()) {
                writer.write("**Pinned:** Yes");
                writer.newLine();
            }
            
            writer.newLine();
            writer.write("---");
            writer.newLine();
            writer.newLine();
            
            // Content
            writer.write(note.getContent());
            
            logger.info("Note exported to Markdown: " + filePath);
        }
    }
    
    /**
     * Export multiple notes to a single Markdown file.
     */
    public static void exportAllToMarkdown(List<Note> notes, String filePath) throws IOException {
        Path path = Paths.get(filePath);
        
        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
            writer.write("# My Notes Export");
            writer.newLine();
            writer.newLine();
            writer.write("Exported on: " + java.time.LocalDateTime.now().format(formatter));
            writer.newLine();
            writer.write("Total notes: " + notes.size());
            writer.newLine();
            writer.newLine();
            writer.write("---");
            writer.newLine();
            writer.newLine();
            
            for (Note note : notes) {
                // Title
                writer.write("## " + note.getTitle());
                writer.newLine();
                writer.newLine();
                
                // Metadata
                writer.write("*Created: " + note.getCreatedAt().format(formatter) + "*  ");
                writer.newLine();
                writer.write("*Updated: " + note.getUpdatedAt().format(formatter) + "*  ");
                writer.newLine();
                
                // Tags
                if (!note.getTags().isEmpty()) {
                    writer.write("*Tags: " + String.join(", ", note.getTags()) + "*  ");
                    writer.newLine();
                }
                
                if (note.isPinned()) {
                    writer.write("*📌 Pinned*  ");
                    writer.newLine();
                }
                
                writer.newLine();
                
                // Content
                writer.write(note.getContent());
                writer.newLine();
                writer.newLine();
                writer.write("---");
                writer.newLine();
                writer.newLine();
            }
            
            logger.info("All notes exported to Markdown: " + filePath);
        }
    }
    
    /**
     * Export a single note to HTML format.
     */
    public static void exportToHtml(Note note, String filePath) throws IOException {
        Path path = Paths.get(filePath);
        
        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
            writer.write("<!DOCTYPE html>");
            writer.newLine();
            writer.write("<html>");
            writer.newLine();
            writer.write("<head>");
            writer.newLine();
            writer.write("    <meta charset=\"UTF-8\">");
            writer.newLine();
            writer.write("    <title>" + escapeHtml(note.getTitle()) + "</title>");
            writer.newLine();
            writer.write("    <style>");
            writer.newLine();
            writer.write("        body { font-family: 'Segoe UI', Arial, sans-serif; max-width: 800px; margin: 40px auto; padding: 20px; background: #f5f5f5; }");
            writer.newLine();
            writer.write("        .note { background: white; padding: 30px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }");
            writer.newLine();
            writer.write("        h1 { color: #333; margin-top: 0; }");
            writer.newLine();
            writer.write("        .metadata { color: #666; font-size: 14px; margin-bottom: 20px; }");
            writer.newLine();
            writer.write("        .tags { display: inline-block; background: #4CAF50; color: white; padding: 4px 12px; border-radius: 12px; margin-right: 8px; font-size: 12px; }");
            writer.newLine();
            writer.write("        .content { line-height: 1.6; color: #333; }");
            writer.newLine();
            writer.write("        .pinned { color: #E53935; font-weight: bold; }");
            writer.newLine();
            writer.write("    </style>");
            writer.newLine();
            writer.write("</head>");
            writer.newLine();
            writer.write("<body>");
            writer.newLine();
            writer.write("    <div class=\"note\">");
            writer.newLine();
            writer.write("        <h1>" + escapeHtml(note.getTitle()) + "</h1>");
            writer.newLine();
            writer.write("        <div class=\"metadata\">");
            writer.newLine();
            writer.write("            <p><strong>Created:</strong> " + note.getCreatedAt().format(formatter) + "</p>");
            writer.newLine();
            writer.write("            <p><strong>Updated:</strong> " + note.getUpdatedAt().format(formatter) + "</p>");
            writer.newLine();
            
            if (!note.getTags().isEmpty()) {
                writer.write("            <p><strong>Tags:</strong> ");
                for (String tag : note.getTags()) {
                    writer.write("<span class=\"tags\">" + escapeHtml(tag) + "</span>");
                }
                writer.write("</p>");
                writer.newLine();
            }
            
            if (note.isPinned()) {
                writer.write("            <p class=\"pinned\">📌 Pinned</p>");
                writer.newLine();
            }
            
            writer.write("        </div>");
            writer.newLine();
            writer.write("        <div class=\"content\">");
            writer.newLine();
            writer.write("            <p>" + escapeHtml(note.getContent()).replace("\n", "<br>") + "</p>");
            writer.newLine();
            writer.write("        </div>");
            writer.newLine();
            writer.write("    </div>");
            writer.newLine();
            writer.write("</body>");
            writer.newLine();
            writer.write("</html>");
            
            logger.info("Note exported to HTML: " + filePath);
        }
    }
    
    private static String escapeHtml(String text) {
        return text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
