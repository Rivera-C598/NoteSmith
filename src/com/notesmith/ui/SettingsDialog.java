package com.notesmith.ui;

import com.notesmith.config.AppConfig;
import com.notesmith.config.AppStyles;

import javax.swing.*;
import java.awt.*;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Settings dialog for configuring API keys and preferences.
 */
public class SettingsDialog extends JDialog {
    
    private JTextField apiKeyField;
    private JCheckBox enableAICheckbox;
    private boolean saved = false;
    
    public SettingsDialog(Frame parent) {
        super(parent, "Settings", true);
        initUI();
    }
    
    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setSize(600, 500);
        setLocationRelativeTo(getParent());
        
        // Main panel
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Color.WHITE);
        
        // Title
        JLabel titleLabel = new JLabel("Settings");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainPanel.add(titleLabel);
        mainPanel.add(Box.createVerticalStrut(20));
        
        // AI Settings Section
        JPanel aiPanel = createSection("AI Features (Optional)");
        
        // Enable AI checkbox
        enableAICheckbox = new JCheckBox("Enable AI Features");
        enableAICheckbox.setSelected(AppConfig.isAIEnabled());
        enableAICheckbox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        enableAICheckbox.setBackground(Color.WHITE);
        enableAICheckbox.addActionListener(e -> apiKeyField.setEnabled(enableAICheckbox.isSelected()));
        aiPanel.add(enableAICheckbox);
        aiPanel.add(Box.createVerticalStrut(10));
        
        // Info text
        JTextArea infoText = new JTextArea(
            "AI features include smart summaries, content analysis, and intelligent note linking.\n" +
            "To use AI features, you need a free Google Gemini API key."
        );
        infoText.setEditable(false);
        infoText.setLineWrap(true);
        infoText.setWrapStyleWord(true);
        infoText.setBackground(new Color(240, 248, 255));
        infoText.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        infoText.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        aiPanel.add(infoText);
        aiPanel.add(Box.createVerticalStrut(15));
        
        // API Key label and field
        JLabel apiKeyLabel = new JLabel("Google Gemini API Key (Optional):");
        apiKeyLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        apiKeyLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        aiPanel.add(apiKeyLabel);
        aiPanel.add(Box.createVerticalStrut(5));
        
        apiKeyField = new JTextField(AppConfig.getGeminiApiKey());
        apiKeyField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        apiKeyField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        apiKeyField.setEnabled(enableAICheckbox.isSelected());
        aiPanel.add(apiKeyField);
        aiPanel.add(Box.createVerticalStrut(10));
        
        // Get API Key button
        JButton getKeyButton = new JButton("How to Get Free API Key");
        getKeyButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        getKeyButton.setBackground(new Color(66, 133, 244));
        getKeyButton.setForeground(Color.WHITE);
        getKeyButton.setFocusPainted(false);
        getKeyButton.setBorderPainted(false);
        getKeyButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        getKeyButton.addActionListener(e -> showApiKeyInstructions());
        aiPanel.add(getKeyButton);
        
        mainPanel.add(aiPanel);
        mainPanel.add(Box.createVerticalStrut(20));
        
        // Note about mock mode
        JTextArea mockNote = new JTextArea(
            "Note: If you don't provide an API key, AI features will run in demo mode " +
            "with simulated responses. The app works perfectly fine without AI!"
        );
        mockNote.setEditable(false);
        mockNote.setLineWrap(true);
        mockNote.setWrapStyleWord(true);
        mockNote.setBackground(new Color(255, 250, 230));
        mockNote.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mockNote.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        mockNote.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainPanel.add(mockNote);
        
        add(mainPanel, BorderLayout.CENTER);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));
        
        JButton saveButton = new JButton("Save");
        saveButton.setBackground(AppStyles.ACCENT);
        saveButton.setForeground(Color.WHITE);
        saveButton.setFocusPainted(false);
        saveButton.addActionListener(e -> saveSettings());
        
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> dispose());
        
        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);
        
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createSection(String title) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        panel.setBackground(Color.WHITE);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(15));
        
        return panel;
    }
    
    private void showApiKeyInstructions() {
        JDialog instructionsDialog = new JDialog(this, "How to Get a Free API Key", true);
        instructionsDialog.setSize(650, 500);
        instructionsDialog.setLocationRelativeTo(this);
        instructionsDialog.setLayout(new BorderLayout());
        
        JTextArea instructions = new JTextArea();
        instructions.setText(
            "How to Get Your Free Google Gemini API Key\n" +
            "==========================================\n\n" +
            "1. Go to Google AI Studio:\n" +
            "   https://aistudio.google.com/app/apikey\n\n" +
            "2. Sign in with your Google account\n" +
            "   (Any Gmail account works - free!)\n\n" +
            "3. Click \"Create API Key\"\n\n" +
            "4. Select \"Create API key in new project\" or choose an existing project\n\n" +
            "5. Copy your API key (it looks like: AIzaSyA...)\n\n" +
            "6. Paste it in the API Key field in Settings\n\n" +
            "7. Click Save\n\n" +
            "That's it! 🎉\n\n" +
            "Free Tier Limits:\n" +
            "• 15 requests per minute\n" +
            "• 1 million tokens per day\n" +
            "• More than enough for personal note-taking!\n\n" +
            "Privacy & Security:\n" +
            "✓ Your API key is stored locally on YOUR computer only\n" +
            "✓ Your notes are sent to Google Gemini for AI processing\n" +
            "✓ You can delete your key anytime from Settings\n" +
            "✓ The app works fine without AI features too!\n\n" +
            "Need help? Check the full guide at:\n" +
            "https://ai.google.dev/gemini-api/docs/api-key"
        );
        instructions.setEditable(false);
        instructions.setLineWrap(true);
        instructions.setWrapStyleWord(true);
        instructions.setMargin(new Insets(15, 15, 15, 15));
        instructions.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        
        JScrollPane scrollPane = new JScrollPane(instructions);
        instructionsDialog.add(scrollPane, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton openBrowserButton = new JButton("Open Google AI Studio");
        openBrowserButton.setBackground(new Color(66, 133, 244));
        openBrowserButton.setForeground(Color.WHITE);
        openBrowserButton.setFocusPainted(false);
        openBrowserButton.addActionListener(e -> {
            try {
                Desktop.getDesktop().browse(new java.net.URI("https://aistudio.google.com/app/apikey"));
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(instructionsDialog,
                    "Please visit: https://aistudio.google.com/app/apikey",
                    "Open Browser",
                    JOptionPane.INFORMATION_MESSAGE);
            }
        });
        
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> instructionsDialog.dispose());
        
        buttonPanel.add(openBrowserButton);
        buttonPanel.add(closeButton);
        instructionsDialog.add(buttonPanel, BorderLayout.SOUTH);
        
        instructionsDialog.setVisible(true);
    }
    
    private void saveSettings() {
        try {
            // Save to user's home directory config
            String userHome = System.getProperty("user.home");
            java.io.File configDir = new java.io.File(userHome, ".notesmith");
            configDir.mkdirs();
            
            java.io.File configFile = new java.io.File(configDir, "config.properties");
            
            Properties props = new Properties();
            
            // Load existing properties if file exists
            if (configFile.exists()) {
                try (java.io.FileInputStream in = new java.io.FileInputStream(configFile)) {
                    props.load(in);
                }
            }
            
            // Update properties
            props.setProperty("ai.enabled", String.valueOf(enableAICheckbox.isSelected()));
            
            String apiKey = apiKeyField.getText().trim();
            if (!apiKey.isEmpty()) {
                props.setProperty("ai.gemini.api.key", apiKey);
            } else {
                props.remove("ai.gemini.api.key");
            }
            
            // Save to file
            try (FileOutputStream out = new FileOutputStream(configFile)) {
                props.store(out, "NoteSmith User Configuration");
            }
            
            saved = true;
            
            JOptionPane.showMessageDialog(this,
                "Settings saved successfully!\n\n" +
                "⚠️ Please restart NoteSmith for changes to take effect.",
                "Settings Saved",
                JOptionPane.INFORMATION_MESSAGE);
            
            dispose();
            
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                "Failed to save settings: " + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public boolean wasSaved() {
        return saved;
    }
}
