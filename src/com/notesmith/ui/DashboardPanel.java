package com.notesmith.ui;

import com.notesmith.config.AppStyles;
import com.notesmith.config.UIConstants;
import com.notesmith.exception.PersistenceException;
import com.notesmith.exception.ValidationException;
import com.notesmith.model.Note;
import com.notesmith.model.TextNote;
import com.notesmith.model.User;
import com.notesmith.persistence.NoteRepository;
import com.notesmith.ui.components.*;
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
import java.util.List;

public class DashboardPanel extends CPanel {

    public interface LogoutListener {
        void onLogout();
    }

    // ==== FIELDS (class members) ====
    private User user;
    private NoteRepository noteRepo;

    private DefaultListModel<Note> listModel;
    private JList<Note> noteList;

    private CTextField titleField;
    private CTextArea contentArea;
    private JEditorPane previewPane;
    private JLabel messageLabel;
    private CButton saveBtn;

    private Note currentNote; // null = adding new note

    public DashboardPanel(User user, NoteRepository noteRepo, LogoutListener listener) {
        this.user = user;
        this.noteRepo = noteRepo;

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
                    setText(((Note) value).display());
                }
                setBackground(isSelected ? AppStyles.ACCENT : AppStyles.BG_CARD);
                setForeground(AppStyles.TEXT_PRIMARY);
                setFont(AppStyles.fontNormal());
                return comp;
            }
        });

        JPanel left = new JPanel(new BorderLayout());
        left.setBackground(AppStyles.BG_MAIN);
// slightly smaller right padding so gap in middle isn't huge
        left.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 4));
        left.add(CLabel.secondary("Your Notes"), BorderLayout.NORTH);

        JScrollPane listScroll = new JScrollPane(noteList);
        listScroll.getViewport().setBackground(AppStyles.BG_MAIN);
        left.add(listScroll, BorderLayout.CENTER);

        CButton deleteBtn = CButton.danger("Delete Selected");
        deleteBtn.addActionListener(e -> deleteSelectedWithConfirmation());
        left.add(deleteBtn, BorderLayout.SOUTH);

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
        // New Note button: clears editor and goes back to "Add Note" mode
        CButton newNoteBtn = CButton.danger("New Note");
        newNoteBtn.addActionListener(e -> clearEditor());
        buttonBar.add(newNoteBtn);

        fullWidth(buttonBar);
        fields.add(buttonBar);



// Save/Add button: creates or updates depending on currentNote
        saveBtn = new CButton("Add Note");
        saveBtn.addActionListener(e -> saveNote());
        buttonBar.add(saveBtn);

        fields.add(buttonBar);

        right.add(fields, BorderLayout.CENTER);

        splitPane.setLeftComponent(left);
        splitPane.setRightComponent(right);

        add(splitPane, BorderLayout.CENTER);
        
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
            List<Note> notes = noteRepo.findAll();
            notes.forEach(listModel::addElement);
        } catch (PersistenceException e) {
            messageLabel.setText("Failed to load notes: " + e.getMessage());
            messageLabel.setForeground(AppStyles.ACCENT_DANGER);
        }
    }

    // Save note: handles both Add and Update depending on currentNote
    private void saveNote() {
        String title = titleField.getText().trim();
        String content = contentArea.getText().trim();

        try {
            // Validate title
            ValidationUtils.validateNoteTitle(title);
            
            if (currentNote == null) {
                // create new note
                Note note = new TextNote(title, content);
                noteRepo.save(note);
                listModel.add(0, note); // add to top
                messageLabel.setText("Note added successfully!");
                messageLabel.setForeground(AppStyles.ACCENT);
            } else {
                // update existing note
                currentNote.setTitle(title);
                currentNote.setContent(content);
                noteRepo.save(currentNote);
                noteList.repaint();
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

}
