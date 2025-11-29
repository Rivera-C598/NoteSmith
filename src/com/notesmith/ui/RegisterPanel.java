package com.notesmith.ui;

import com.notesmith.config.AppStyles;
import com.notesmith.exception.ValidationException;
import com.notesmith.persistence.JdbcUserRepository;
import com.notesmith.security.PasswordHasher;
import com.notesmith.ui.components.*;
import com.notesmith.util.ValidationUtils;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;

public class RegisterPanel extends CPanel {

    public interface RegisterListener {
        void onBackToLogin();
    }

    private final JdbcUserRepository userRepo;
    private final RegisterListener listener;
    private final CTextField usernameField;
    private final JPasswordField passwordField;
    private final JPasswordField confirmField;
    private final JLabel messageLabel;
    private final JProgressBar strengthBar;
    private final JLabel strengthLabel;

    public RegisterPanel(JdbcUserRepository userRepo, RegisterListener listener) {
        this.userRepo = userRepo;
        this.listener = listener;

        setLayout(new GridBagLayout());
        setBackground(AppStyles.BG_MAIN);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;

        CPanel card = new CPanel();
        card.setLayout(new GridBagLayout());
        card.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 0;

        card.add(CLabel.title("Create Account"), c);

        c.gridy++;
        card.add(CLabel.secondary("Register for NoteSmith"), c);

        c.gridy++;
        card.add(new CLabel("Username"), c);
        c.gridy++;
        usernameField = new CTextField(20);
        card.add(usernameField, c);

        c.gridy++;
        card.add(new CLabel("Password"), c);
        c.gridy++;
        passwordField = new JPasswordField(20);
        passwordField.setBackground(AppStyles.BG_CARD);
        passwordField.setForeground(AppStyles.TEXT_PRIMARY);
        passwordField.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
        card.add(passwordField, c);

        c.gridy++;
        card.add(new CLabel("Confirm Password"), c);
        c.gridy++;
        confirmField = new JPasswordField(20);
        confirmField.setBackground(AppStyles.BG_CARD);
        confirmField.setForeground(AppStyles.TEXT_PRIMARY);
        confirmField.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
        card.add(confirmField, c);

        c.gridy++;
        strengthLabel = CLabel.secondary("Password Strength:");
        card.add(strengthLabel, c);
        
        c.gridy++;
        strengthBar = new JProgressBar(0, 100);
        strengthBar.setStringPainted(true);
        strengthBar.setForeground(AppStyles.ACCENT_DANGER);
        card.add(strengthBar, c);
        
        // Add password strength listener
        passwordField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { updatePasswordStrength(); }
            public void removeUpdate(DocumentEvent e) { updatePasswordStrength(); }
            public void changedUpdate(DocumentEvent e) { updatePasswordStrength(); }
        });

        c.gridy++;
        messageLabel = CLabel.secondary("");
        card.add(messageLabel, c);

        c.gridy++;
        CButton registerBtn = new CButton("Register");
        registerBtn.addActionListener(e -> register());
        card.add(registerBtn, c);

        c.gridy++;
        CButton backBtn = CButton.danger("Back to Login");
        backBtn.addActionListener(e -> listener.onBackToLogin());
        card.add(backBtn, c);

        gbc.gridy = 0;
        add(card, gbc);
    }

    private void updatePasswordStrength() {
        String password = new String(passwordField.getPassword());
        int strength = ValidationUtils.getPasswordStrength(password);
        strengthBar.setValue(strength);
        
        if (strength < 40) {
            strengthBar.setForeground(AppStyles.ACCENT_DANGER);
            strengthLabel.setText("Password Strength: Weak");
        } else if (strength < 70) {
            strengthBar.setForeground(new Color(0xFF9800)); // Orange
            strengthLabel.setText("Password Strength: Medium");
        } else {
            strengthBar.setForeground(AppStyles.ACCENT);
            strengthLabel.setText("Password Strength: Strong");
        }
    }
    
    private void register() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirm = new String(confirmField.getPassword());

        if (username.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
            messageLabel.setText("All fields are required.");
            messageLabel.setForeground(AppStyles.ACCENT_DANGER);
            return;
        }

        if (!password.equals(confirm)) {
            messageLabel.setText("Passwords do not match.");
            messageLabel.setForeground(AppStyles.ACCENT_DANGER);
            return;
        }

        try {
            // Validate username format
            ValidationUtils.validateUsername(username);
            
            // Validate password strength
            ValidationUtils.validatePassword(password);
            
            if (userRepo.findByUsername(username) != null) {
                messageLabel.setText("Username already taken.");
                messageLabel.setForeground(AppStyles.ACCENT_DANGER);
                return;
            }

            String hash = PasswordHasher.hash(password);
            userRepo.createUser(username, hash);
            messageLabel.setText("Registration successful! You can log in now.");
            messageLabel.setForeground(AppStyles.ACCENT);

        } catch (ValidationException ex) {
            messageLabel.setText(ex.getMessage());
            messageLabel.setForeground(AppStyles.ACCENT_DANGER);
        } catch (Exception ex) {
            messageLabel.setText("Error: " + ex.getMessage());
            messageLabel.setForeground(AppStyles.ACCENT_DANGER);
        }
    }
}
