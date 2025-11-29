package com.notesmith.ui;

import com.notesmith.ai.ContentAnalyzer;
import com.notesmith.ai.SmartLinkingService;
import com.notesmith.ai.SummarizationService;
import com.notesmith.ai.models.RelatedNote;
import com.notesmith.config.AppConfig;
import com.notesmith.config.AppStyles;
import com.notesmith.config.UIConstants;
import com.notesmith.exception.PersistenceException;
import com.notesmith.exception.ValidationException;
import com.notesmith.model.Note;
import com.notesmith.model.TextNote;
import com.notesmith.model.User;
import com.notesmith.persistence.NoteRepository;
import com.notesmith.ui.components.*;
import com.notesmith.util.ExportUtils;
import com.notesmith.util.ValidationUtils;

import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.undo.UndoManager;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.CannotRedoException;
import javax.swing.KeyStroke;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.AbstractAction;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DashboardPanel extends CPanel {

    public interface LogoutListener {
        void onLogout();
    }

    // ==== FIELDS (class members) ====
    private User user;
    private NoteRepository noteRepo;

    private DefaultListModel<Note> listModel;
    private DefaultListModel<Note> filteredListModel;
    private JList<Note> noteList;
    private List<Note> allNotes;

    private CTextField titleField;
    private CTextField tagsField;
    private CTextArea contentArea;
    private JEditorPane previewPane;
    private JLabel messageLabel;
    private CButton saveBtn;
    private CTextField searchField;
    private JCheckBox pinCheckbox;

    private Note currentNote; // null = adding new note
    
    // AI Services
    private SmartLinkingService smartLinkingService;
    private SummarizationService summarizationService;
    private ContentAnalyzer contentAnalyzer;
    
    // AI UI Components
    private JTextArea aiSummaryArea;
    private DefaultListModel<RelatedNote> relatedNotesModel;
    private JList<RelatedNote> relatedNotesList;
    private JLabel aiStatusLabel;

    public DashboardPanel(User user, NoteRepository noteRepo, LogoutListener listener) {
        this.user = user;
        this.noteRepo = noteRepo;
        
        // Initialize AI services if enabled
        if (AppConfig.isAIEnabled()) {
            this.smartLinkingService = new SmartLinkingService();
            this.summarizationService = new SummarizationService();
            this.contentAnalyzer = new ContentAnalyzer();
        }

        setLayout(new BorderLayout());
        setBackground(AppStyles.BG_MAIN);

        // ===== TOP BAR =====
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(AppStyles.BG_CARD);
        topBar.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));

        // Wrap app title + welcome text in one panel with spacing
        JPanel titleWrap = new JPanel();
        titleWrap.setOpaque(false);
        titleWrap.setLayout(new BoxLayout(titleWrap, BoxLayout.X_AXIS));

        titleWrap.add(CLabel.title("NoteSmith"));
        titleWrap.add(Box.createHorizontalStrut(16));
        titleWrap.add(CLabel.secondary("Welcome, " + user.getUsername()));

        topBar.add(titleWrap, BorderLayout.WEST);

        CButton logoutBtn = CButton.danger("Logout");
        logoutBtn.addActionListener(e -> listener.onLogout());
        topBar.add(logoutBtn, BorderLayout.EAST);

        add(topBar, BorderLayout.NORTH);

        // ===== CENTER SPLIT =====
        JSplitPane splitPane = new JSplitPane();
        splitPane.setDividerLocation(UIConstants.NOTES_LIST_WIDTH);
        splitPane.setContinuousLayout(true);
        splitPane.setDividerSize(UIConstants.DIVIDER_SIZE);
        splitPane.setBorder(null);

        // ===== LEFT: NOTE LIST =====
        listModel = new DefaultListModel<>();
        noteList = new JList<>(listModel);
        noteList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        noteList.setBackground(AppStyles.BG_MAIN);
        noteList.setSelectionBackground(AppStyles.ACCENT);
        noteList.setSelectionForeground(AppStyles.TEXT_PRIMARY);
        noteList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                                                          int index, boolean isSelected, boolean cellHasFocus) {
                Component comp = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Note) {
                    Note note = (Note) value;
                    String display = note.display();
                    
                    // Add pin indicator
                    if (note.isPinned()) {
                        display = "📌 " + display;
                    }
                    
                    // Add tags
                    if (!note.getTags().isEmpty()) {
                        display += " [" + String.join(", ", note.getTags()) + "]";
                    }
                    
                    setText(display);
                }
                setBackground(isSelected ? AppStyles.ACCENT : AppStyles.BG_CARD);
                setForeground(AppStyles.TEXT_PRIMARY);
                setFont(AppStyles.fontNormal());
                return comp;
            }
        });

        JPanel left = new JPanel(new BorderLayout());
        left.setBackground(AppStyles.BG_MAIN);
        left.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 4));
        
        // Top section with title and search
        JPanel leftTop = new JPanel();
        leftTop.setLayout(new BoxLayout(leftTop, BoxLayout.Y_AXIS));
        leftTop.setOpaque(false);
        
        JLabel notesLabel = CLabel.secondary("Your Notes");
        notesLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        leftTop.add(notesLabel);
        
        leftTop.add(Box.createVerticalStrut(8));
        
        // Search field
        searchField = new CTextField(20);
        searchField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        searchField.setAlignmentX(Component.LEFT_ALIGNMENT);
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filterNotes(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filterNotes(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filterNotes(); }
        });
        
        JPanel searchPanel = new JPanel(new BorderLayout());
        searchPanel.setOpaque(false);
        searchPanel.add(new CLabel("🔍 Search: "), BorderLayout.WEST);
        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        searchPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        leftTop.add(searchPanel);
        
        left.add(leftTop, BorderLayout.NORTH);

        JScrollPane listScroll = new JScrollPane(noteList);
        listScroll.getViewport().setBackground(AppStyles.BG_MAIN);
        left.add(listScroll, BorderLayout.CENTER);

        // Bottom buttons panel
        JPanel bottomButtons = new JPanel(new GridLayout(3, 1, 4, 4));
        bottomButtons.setOpaque(false);
        
        CButton exportBtn = new CButton("Export Selected");
        exportBtn.addActionListener(e -> exportSelected());
        bottomButtons.add(exportBtn);
        
        CButton exportAllBtn = new CButton("Export All");
        exportAllBtn.addActionListener(e -> exportAll());
        bottomButtons.add(exportAllBtn);
        
        CButton deleteBtn = CButton.danger("Delete Selected");
        deleteBtn.addActionListener(e -> deleteSelectedWithConfirmation());
        bottomButtons.add(deleteBtn);
        
        left.add(bottomButtons, BorderLayout.SOUTH);

        // ===== RIGHT: EDITOR + PREVIEW =====
        JPanel right = new JPanel(new BorderLayout());
        right.setBackground(AppStyles.BG_MAIN);
        right.setBorder(BorderFactory.createEmptyBorder(8, 4, 8, 8));

        JPanel fields = new JPanel();
        fields.setLayout(new BoxLayout(fields, BoxLayout.Y_AXIS));
        fields.setOpaque(false);

        JLabel titleLabel = CLabel.secondary("Title");
        fullWidth(titleLabel);
        fields.add(titleLabel);

        titleField = new CTextField(30);
        titleField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        fullWidth(titleField);
        fields.add(titleField);

        installUndoRedo(titleField);

        fields.add(Box.createVerticalStrut(8));
        
        // Tags field
        JLabel tagsLabel = CLabel.secondary("Tags (comma-separated)");
        fullWidth(tagsLabel);
        fields.add(tagsLabel);
        
        tagsField = new CTextField(30);
        tagsField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        fullWidth(tagsField);
        fields.add(tagsField);

        fields.add(Box.createVerticalStrut(8));

        JLabel contentLabel = CLabel.secondary("Content");
        fullWidth(contentLabel);
        fields.add(contentLabel);


        // formatting toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        toolbar.setOpaque(false);

        CButton boldBtn = new CButton("B");
        boldBtn.setFont(AppStyles.fontNormal().deriveFont(Font.BOLD));
        boldBtn.addActionListener(e -> wrapSelection("**", "**"));

        CButton italicBtn = new CButton("I");
        italicBtn.setFont(AppStyles.fontNormal().deriveFont(Font.ITALIC));
        italicBtn.addActionListener(e -> wrapSelection("_", "_"));

        CButton bulletBtn = new CButton("•");
        bulletBtn.addActionListener(e -> insertAtLineStart("- "));

        toolbar.add(boldBtn);
        toolbar.add(italicBtn);
        toolbar.add(bulletBtn);

        fullWidth(toolbar);
        fields.add(toolbar);

        // editor
        contentArea = new CTextArea(10, 30);
        JScrollPane contentScroll = new JScrollPane(contentArea);

        installUndoRedo(contentArea);

        previewPane = new JEditorPane();
        previewPane.setContentType("text/html");
        previewPane.setEditable(false);
        previewPane.setBackground(AppStyles.BG_CARD);
        previewPane.setForeground(AppStyles.TEXT_PRIMARY);
        previewPane.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
        JScrollPane previewScroll = new JScrollPane(previewPane);

        JSplitPane editorPreviewSplit =
                new JSplitPane(JSplitPane.VERTICAL_SPLIT, contentScroll, previewScroll);
        editorPreviewSplit.setResizeWeight(UIConstants.EDITOR_PREVIEW_SPLIT_RATIO / 100.0);
        editorPreviewSplit.setBorder(null);

        // make the whole editor+preview block same width as title
        fullWidth(editorPreviewSplit);
        fields.add(editorPreviewSplit);


        // live preview
        contentArea.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { updatePreview(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { updatePreview(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { updatePreview(); }
        });
        updatePreview();

// message + buttons
        fields.add(Box.createVerticalStrut(8));
        messageLabel = CLabel.secondary("");
        fullWidth(messageLabel);
        fields.add(messageLabel);

        fields.add(Box.createVerticalStrut(4));
        JPanel buttonBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttonBar.setOpaque(false);

        // add New Note + Save buttons here
        // Pin checkbox
        pinCheckbox = new JCheckBox("📌 Pin this note");
        pinCheckbox.setOpaque(false);
        pinCheckbox.setForeground(AppStyles.TEXT_PRIMARY);
        pinCheckbox.setFont(AppStyles.fontNormal());
        fullWidth(pinCheckbox);
        fields.add(pinCheckbox);
        
        fields.add(Box.createVerticalStrut(4));
        
        // New Note button: clears editor and goes back to "Add Note" mode
        CButton newNoteBtn = CButton.danger("New Note");
        newNoteBtn.addActionListener(e -> clearEditor());
        buttonBar.add(newNoteBtn);

// Save/Add button: creates or updates depending on currentNote
        saveBtn = new CButton("Add Note");
        saveBtn.addActionListener(e -> saveNote());
        buttonBar.add(saveBtn);

        fullWidth(buttonBar);
        fields.add(buttonBar);

        right.add(fields, BorderLayout.CENTER);

        // ===== RIGHT PANEL: AI Insights =====
        JPanel aiPanel = createAIPanel();
        
        // Three-way split: notes list | editor | AI insights
        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, splitPane, aiPanel);
        mainSplit.setDividerLocation(700);
        mainSplit.setContinuousLayout(true);
        mainSplit.setDividerSize(UIConstants.DIVIDER_SIZE);
        
        splitPane.setLeftComponent(left);
        splitPane.setRightComponent(right);

        add(mainSplit, BorderLayout.CENTER);
        
        // Setup keyboard shortcuts
        setupKeyboardShortcuts();

        // 👉 AFTER everything is created, THEN wire the list click behavior
        noteList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                Note selected = noteList.getSelectedValue();
                if (selected != null) {
                    currentNote = selected;
                    titleField.setText(selected.getTitle());
                    contentArea.setText(selected.getContent());
                    tagsField.setText(String.join(", ", selected.getTags()));
                    pinCheckbox.setSelected(selected.isPinned());
                    saveBtn.setText("Save Changes");
                    updatePreview();
                }
            }
        });

        loadNotes();
    }

    private void loadNotes() {
        listModel.clear();
        try {
            allNotes = noteRepo.findAll();
            allNotes.forEach(listModel::addElement);
        } catch (PersistenceException e) {
            messageLabel.setText("Failed to load notes: " + e.getMessage());
            messageLabel.setForeground(AppStyles.ACCENT_DANGER);
        }
    }
    
    private void filterNotes() {
        String query = searchField.getText().toLowerCase().trim();
        listModel.clear();
        
        if (query.isEmpty()) {
            // Show all notes
            allNotes.forEach(listModel::addElement);
        } else {
            // Filter notes by title or content
            allNotes.stream()
                .filter(note -> note.getTitle().toLowerCase().contains(query) ||
                               note.getContent().toLowerCase().contains(query))
                .forEach(listModel::addElement);
        }
    }

    // Save note: handles both Add and Update depending on currentNote
    private void saveNote() {
        String title = titleField.getText().trim();
        String content = contentArea.getText().trim();
        String tagsText = tagsField.getText().trim();
        boolean pinned = pinCheckbox.isSelected();

        try {
            // Validate title
            ValidationUtils.validateNoteTitle(title);
            
            if (currentNote == null) {
                // create new note
                Note note = new TextNote(title, content);
                
                // Add tags
                if (!tagsText.isEmpty()) {
                    String[] tags = tagsText.split(",");
                    for (String tag : tags) {
                        note.addTag(tag.trim());
                    }
                }
                
                note.setPinned(pinned);
                noteRepo.save(note);
                
                // Reload to maintain sort order (pinned first)
                loadNotes();
                messageLabel.setText("Note added successfully!");
                messageLabel.setForeground(AppStyles.ACCENT);
            } else {
                // update existing note
                currentNote.setTitle(title);
                currentNote.setContent(content);
                
                // Update tags
                List<String> newTags = new ArrayList<>();
                if (!tagsText.isEmpty()) {
                    String[] tags = tagsText.split(",");
                    for (String tag : tags) {
                        String trimmed = tag.trim();
                        if (!trimmed.isEmpty()) {
                            newTags.add(trimmed);
                        }
                    }
                }
                currentNote.setTags(newTags);
                currentNote.setPinned(pinned);
                
                noteRepo.save(currentNote);
                
                // Reload to maintain sort order
                loadNotes();
                messageLabel.setText("Note updated successfully!");
                messageLabel.setForeground(AppStyles.ACCENT);
            }

            clearEditor();

        } catch (ValidationException e) {
            messageLabel.setText(e.getMessage());
            messageLabel.setForeground(AppStyles.ACCENT_DANGER);
        } catch (PersistenceException e) {
            messageLabel.setText("Failed to save note: " + e.getMessage());
            messageLabel.setForeground(AppStyles.ACCENT_DANGER);
        }
    }

    private void clearEditor() {
        currentNote = null;
        titleField.setText("");
        contentArea.setText("");
        tagsField.setText("");
        pinCheckbox.setSelected(false);
        saveBtn.setText("Add Note");
        updatePreview();
        noteList.clearSelection();
    }

    private void deleteSelectedWithConfirmation() {
        Note selected = noteList.getSelectedValue();
        if (selected == null) {
            messageLabel.setText("Select a note to delete.");
            messageLabel.setForeground(AppStyles.ACCENT_DANGER);
            return;
        }
        
        int result = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to delete \"" + selected.getTitle() + "\"?",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        
        if (result == JOptionPane.YES_OPTION) {
            deleteSelected(selected);
        }
    }
    
    private void deleteSelected(Note note) {
        try {
            noteRepo.delete(note.getId());
            listModel.removeElement(note);
            if (currentNote == note) {
                clearEditor();
            }
            messageLabel.setText("Note deleted successfully!");
            messageLabel.setForeground(AppStyles.ACCENT);
        } catch (PersistenceException e) {
            messageLabel.setText("Failed to delete note: " + e.getMessage());
            messageLabel.setForeground(AppStyles.ACCENT_DANGER);
        }
    }

    private void wrapSelection(String prefix, String suffix) {
        String text = contentArea.getText();
        int start = contentArea.getSelectionStart();
        int end = contentArea.getSelectionEnd();

        if (start == end) {
            // no selection: insert prefix+suffix and put caret in the middle
            contentArea.insert(prefix + suffix, start);
            contentArea.setCaretPosition(start + prefix.length());
        } else {
            String selected = text.substring(start, end);
            contentArea.replaceRange(prefix + selected + suffix, start, end);
            contentArea.select(start, start + prefix.length() + selected.length() + suffix.length());
        }
    }

    private void insertAtLineStart(String marker) {
        int caret = contentArea.getCaretPosition();
        try {
            int line = contentArea.getLineOfOffset(caret);
            int lineStart = contentArea.getLineStartOffset(line);
            contentArea.insert(marker, lineStart);
            contentArea.setCaretPosition(caret + marker.length());
        } catch (Exception ignored) {
        }
    }

    private void updatePreview() {
        if (previewPane == null) return;

        String md = contentArea.getText();
        String html = escapeHtml(md);

        // bold: **text**
        html = html.replaceAll("\\*\\*(.+?)\\*\\*", "<b>$1</b>");
        // italic: _text_
        html = html.replaceAll("_(.+?)_", "<i>$1</i>");

        // handle lines & bullet lists
        String[] lines = html.split("\\r?\\n");
        StringBuilder out = new StringBuilder();
        boolean inList = false;

        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.startsWith("- ")) {
                if (!inList) {
                    out.append("<ul>");
                    inList = true;
                }
                String itemText = trimmed.substring(2).trim();
                out.append("<li>").append(itemText.isEmpty() ? "&nbsp;" : itemText).append("</li>");
            } else {
                if (inList) {
                    out.append("</ul>");
                    inList = false;
                }
                if (trimmed.isEmpty()) {
                    out.append("<p>&nbsp;</p>");
                } else {
                    out.append("<p>").append(line).append("</p>");
                }
            }
        }
        if (inList) {
            out.append("</ul>");
        }

        String finalHtml =
                "<html><body style='background:#1E1E1E;color:#FFFFFF;font-family:Segoe UI;font-size:13px;'>"
                        + out
                        + "</body></html>";

        previewPane.setText(finalHtml);
    }

    private String escapeHtml(String text) {
        return text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    private void fullWidth(JComponent c) {
        c.setAlignmentX(Component.LEFT_ALIGNMENT);
        c.setMaximumSize(new Dimension(Integer.MAX_VALUE, c.getPreferredSize().height));
    }

    private void installUndoRedo(JTextComponent textComponent) {
        UndoManager undoManager = new UndoManager();
        Document doc = textComponent.getDocument();
        doc.addUndoableEditListener(undoManager);

        int shortcutMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx(); // Ctrl on Win/Linux, Cmd on Mac

        InputMap im = textComponent.getInputMap(JComponent.WHEN_FOCUSED);
        ActionMap am = textComponent.getActionMap();

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, shortcutMask), "Undo");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_Y, shortcutMask), "Redo");

        am.put("Undo", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                try {
                    if (undoManager.canUndo()) undoManager.undo();
                } catch (CannotUndoException ignored) {}
            }
        });

        am.put("Redo", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                try {
                    if (undoManager.canRedo()) undoManager.redo();
                } catch (CannotRedoException ignored) {}
            }
        });
    }
    
    private void setupKeyboardShortcuts() {
        int shortcutMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
        
        // Ctrl+S to save
        KeyStroke saveKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_S, shortcutMask);
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(saveKeyStroke, "save");
        getActionMap().put("save", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                saveNote();
            }
        });
        
        // Ctrl+N for new note
        KeyStroke newKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_N, shortcutMask);
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(newKeyStroke, "new");
        getActionMap().put("new", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                clearEditor();
            }
        });
        
        // Ctrl+E to export selected
        KeyStroke exportKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_E, shortcutMask);
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(exportKeyStroke, "export");
        getActionMap().put("export", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                exportSelected();
            }
        });
        
        // Delete key to delete selected note
        KeyStroke deleteKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0);
        noteList.getInputMap(JComponent.WHEN_FOCUSED).put(deleteKeyStroke, "delete");
        noteList.getActionMap().put("delete", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                deleteSelectedWithConfirmation();
            }
        });
    }
    
    private void exportSelected() {
        Note selected = noteList.getSelectedValue();
        if (selected == null) {
            messageLabel.setText("Select a note to export.");
            messageLabel.setForeground(AppStyles.ACCENT_DANGER);
            return;
        }
        
        String[] options = {"Markdown (.md)", "HTML (.html)", "Cancel"};
        int choice = JOptionPane.showOptionDialog(
            this,
            "Choose export format:",
            "Export Note",
            JOptionPane.YES_NO_CANCEL_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[0]
        );
        
        if (choice == 2 || choice == JOptionPane.CLOSED_OPTION) {
            return; // Cancelled
        }
        
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Note");
        
        String extension = choice == 0 ? ".md" : ".html";
        String suggestedName = selected.getTitle().replaceAll("[^a-zA-Z0-9-_]", "_") + extension;
        fileChooser.setSelectedFile(new java.io.File(suggestedName));
        
        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            try {
                String filePath = fileChooser.getSelectedFile().getAbsolutePath();
                if (!filePath.endsWith(extension)) {
                    filePath += extension;
                }
                
                if (choice == 0) {
                    ExportUtils.exportToMarkdown(selected, filePath);
                } else {
                    ExportUtils.exportToHtml(selected, filePath);
                }
                
                messageLabel.setText("Note exported successfully!");
                messageLabel.setForeground(AppStyles.ACCENT);
            } catch (IOException e) {
                messageLabel.setText("Export failed: " + e.getMessage());
                messageLabel.setForeground(AppStyles.ACCENT_DANGER);
            }
        }
    }
    
    private void exportAll() {
        if (allNotes.isEmpty()) {
            messageLabel.setText("No notes to export.");
            messageLabel.setForeground(AppStyles.ACCENT_DANGER);
            return;
        }
        
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export All Notes");
        fileChooser.setSelectedFile(new java.io.File("all_notes.md"));
        
        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            try {
                String filePath = fileChooser.getSelectedFile().getAbsolutePath();
                if (!filePath.endsWith(".md")) {
                    filePath += ".md";
                }
                
                ExportUtils.exportAllToMarkdown(allNotes, filePath);
                
                messageLabel.setText("All notes exported successfully!");
                messageLabel.setForeground(AppStyles.ACCENT);
            } catch (IOException e) {
                messageLabel.setText("Export failed: " + e.getMessage());
                messageLabel.setForeground(AppStyles.ACCENT_DANGER);
            }
        }
    }
    
    private JPanel createAIPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(AppStyles.BG_MAIN);
        panel.setBorder(BorderFactory.createEmptyBorder(8, 4, 8, 8));
        
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        
        // Title
        JLabel titleLabel = CLabel.title("🤖 AI Insights");
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(titleLabel);
        
        content.add(Box.createVerticalStrut(16));
        
        // AI Status
        aiStatusLabel = CLabel.secondary(AppConfig.isAIEnabled() ? "AI Ready" : "AI Disabled");
        aiStatusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(aiStatusLabel);
        
        content.add(Box.createVerticalStrut(16));
        
        // Related Notes Section
        JLabel relatedLabel = CLabel.secondary("🔗 Related Notes");
        relatedLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(relatedLabel);
        
        content.add(Box.createVerticalStrut(8));
        
        relatedNotesModel = new DefaultListModel<>();
        relatedNotesList = new JList<>(relatedNotesModel);
        relatedNotesList.setBackground(AppStyles.BG_CARD);
        relatedNotesList.setForeground(AppStyles.TEXT_PRIMARY);
        relatedNotesList.setFont(AppStyles.fontSmall());
        relatedNotesList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                RelatedNote selected = relatedNotesList.getSelectedValue();
                if (selected != null) {
                    // Load the related note
                    noteList.setSelectedValue(selected.getNote(), true);
                }
            }
        });
        
        JScrollPane relatedScroll = new JScrollPane(relatedNotesList);
        relatedScroll.setPreferredSize(new Dimension(200, 150));
        relatedScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));
        relatedScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(relatedScroll);
        
        content.add(Box.createVerticalStrut(8));
        
        CButton findRelatedBtn = new CButton("Find Related");
        findRelatedBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        findRelatedBtn.addActionListener(e -> findRelatedNotes());
        content.add(findRelatedBtn);
        
        content.add(Box.createVerticalStrut(16));
        
        // Summary Section
        JLabel summaryLabel = CLabel.secondary("📝 AI Summary");
        summaryLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(summaryLabel);
        
        content.add(Box.createVerticalStrut(8));
        
        aiSummaryArea = new JTextArea(5, 20);
        aiSummaryArea.setBackground(AppStyles.BG_CARD);
        aiSummaryArea.setForeground(AppStyles.TEXT_PRIMARY);
        aiSummaryArea.setFont(AppStyles.fontSmall());
        aiSummaryArea.setLineWrap(true);
        aiSummaryArea.setWrapStyleWord(true);
        aiSummaryArea.setEditable(false);
        aiSummaryArea.setText("Select a note and click 'Summarize' to generate an AI summary.");
        
        JScrollPane summaryScroll = new JScrollPane(aiSummaryArea);
        summaryScroll.setPreferredSize(new Dimension(200, 120));
        summaryScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        summaryScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(summaryScroll);
        
        content.add(Box.createVerticalStrut(8));
        
        CButton summarizeBtn = new CButton("Summarize");
        summarizeBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        summarizeBtn.addActionListener(e -> summarizeCurrentNote());
        content.add(summarizeBtn);
        
        content.add(Box.createVerticalStrut(16));
        
        // Tag Suggestions
        CButton suggestTagsBtn = new CButton("Suggest Tags");
        suggestTagsBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        suggestTagsBtn.addActionListener(e -> suggestTags());
        content.add(suggestTagsBtn);
        
        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setBackground(AppStyles.BG_MAIN);
        scrollPane.getViewport().setBackground(AppStyles.BG_MAIN);
        scrollPane.setBorder(null);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void findRelatedNotes() {
        if (!AppConfig.isAIEnabled() || smartLinkingService == null) {
            aiStatusLabel.setText("AI is disabled");
            aiStatusLabel.setForeground(AppStyles.ACCENT_DANGER);
            return;
        }
        
        if (currentNote == null) {
            aiStatusLabel.setText("Select a note first");
            aiStatusLabel.setForeground(AppStyles.ACCENT_DANGER);
            return;
        }
        
        aiStatusLabel.setText("Finding related notes...");
        aiStatusLabel.setForeground(AppStyles.TEXT_SECONDARY);
        relatedNotesModel.clear();
        
        // Run in background thread
        new Thread(() -> {
            List<RelatedNote> related = smartLinkingService.findRelatedNotes(currentNote, allNotes);
            
            SwingUtilities.invokeLater(() -> {
                relatedNotesModel.clear();
                for (RelatedNote rn : related) {
                    relatedNotesModel.addElement(rn);
                }
                
                if (related.isEmpty()) {
                    aiStatusLabel.setText("No related notes found");
                } else {
                    aiStatusLabel.setText("Found " + related.size() + " related notes");
                    aiStatusLabel.setForeground(AppStyles.ACCENT);
                }
            });
        }).start();
    }
    
    private void summarizeCurrentNote() {
        if (!AppConfig.isAIEnabled() || summarizationService == null) {
            aiSummaryArea.setText("AI is disabled. Enable it in config.properties");
            return;
        }
        
        if (currentNote == null) {
            aiSummaryArea.setText("Select a note to summarize.");
            return;
        }
        
        aiSummaryArea.setText("Generating summary...");
        aiStatusLabel.setText("Summarizing...");
        
        // Run in background thread
        new Thread(() -> {
            String summary = summarizationService.summarize(currentNote);
            
            SwingUtilities.invokeLater(() -> {
                aiSummaryArea.setText(summary);
                aiStatusLabel.setText("Summary generated");
                aiStatusLabel.setForeground(AppStyles.ACCENT);
            });
        }).start();
    }
    
    private void suggestTags() {
        if (!AppConfig.isAIEnabled() || contentAnalyzer == null) {
            messageLabel.setText("AI is disabled");
            messageLabel.setForeground(AppStyles.ACCENT_DANGER);
            return;
        }
        
        if (currentNote == null) {
            messageLabel.setText("Select a note first");
            messageLabel.setForeground(AppStyles.ACCENT_DANGER);
            return;
        }
        
        aiStatusLabel.setText("Suggesting tags...");
        messageLabel.setText("AI is analyzing content...");
        
        // Run in background thread
        new Thread(() -> {
            List<String> suggestedTags = contentAnalyzer.suggestTags(currentNote);
            
            SwingUtilities.invokeLater(() -> {
                if (suggestedTags.isEmpty()) {
                    messageLabel.setText("No tag suggestions available");
                    messageLabel.setForeground(AppStyles.ACCENT_DANGER);
                } else {
                    String currentTags = tagsField.getText().trim();
                    String newTags = currentTags.isEmpty() ? 
                        String.join(", ", suggestedTags) :
                        currentTags + ", " + String.join(", ", suggestedTags);
                    
                    tagsField.setText(newTags);
                    messageLabel.setText("Tags suggested! Review and save.");
                    messageLabel.setForeground(AppStyles.ACCENT);
                    aiStatusLabel.setText("Tags suggested");
                }
            });
        }).start();
    }

}
