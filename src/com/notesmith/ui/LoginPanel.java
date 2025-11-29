package com.notesmith.ui;

import com.notesmith.config.AppStyles;
import com.notesmith.model.User;
import com.notesmith.persistence.JdbcUserRepository;
import com.notesmith.security.PasswordHasher;
import com.notesmith.ui.components.*;

import javax.swing.*;
import java.awt.*;

public class LoginPanel extends CPanel {

    public interface LoginListener {
        void onLoginSuccess(User user);
        void onRegisterRequested();
    }

    private final JdbcUserRepository userRepo;
    private final LoginListener listener;
    private final CTextField usernameField;
    private final JPasswordField passwordField;
    private final JLabel messageLabel;

    public LoginPanel(JdbcUserRepository userRepo, LoginListener listener) {
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

        card.add(CLabel.title("NoteSmith Login"), c);

        c.gridy++;
        card.add(CLabel.secondary("Sign in to your account"), c);

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
        messageLabel = CLabel.secondary("");
        card.add(messageLabel, c);

        c.gridy++;
        CButton loginBtn = new CButton("Login");
        loginBtn.addActionListener(e -> login());
        card.add(loginBtn, c);

        c.gridy++;
        CButton registerBtn = new CButton("Create an account");
        registerBtn.addActionListener(e -> listener.onRegisterRequested());
        card.add(registerBtn, c);

        gbc.gridy = 0;
        add(card, gbc);
    }

    private void login() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Please enter username and password.");
            messageLabel.setForeground(AppStyles.ACCENT_DANGER);
            return;
        }

        try {
            User user = userRepo.findByUsername(username);
            if (user == null) {
                messageLabel.setText("User not found.");
                messageLabel.setForeground(AppStyles.ACCENT_DANGER);
                return;
            }

            if (!PasswordHasher.verify(password, user.getPasswordHash())) {
                messageLabel.setText("Invalid password.");
                messageLabel.setForeground(AppStyles.ACCENT_DANGER);
                return;
            }

            messageLabel.setText("Login successful!");
            messageLabel.setForeground(AppStyles.ACCENT);
            listener.onLoginSuccess(user);

        } catch (Exception ex) {
            messageLabel.setText("Error: " + ex.getMessage());
            messageLabel.setForeground(AppStyles.ACCENT_DANGER);
        }
    }
}
