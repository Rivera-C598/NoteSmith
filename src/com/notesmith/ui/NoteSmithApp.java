package com.notesmith.ui;

import com.notesmith.config.AppStyles;
import com.notesmith.config.UIConstants;
import com.notesmith.model.User;
import com.notesmith.persistence.*;

import javax.swing.*;
import java.awt.*;

public class NoteSmithApp extends JFrame
        implements LoginPanel.LoginListener,
        RegisterPanel.RegisterListener,
        DashboardPanel.LogoutListener {

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cardPanel = new JPanel(cardLayout);

    private final JdbcUserRepository userRepo = new JdbcUserRepository();

    private LoginPanel loginPanel;
    private RegisterPanel registerPanel;
    private DashboardPanel dashboardPanel;

    public NoteSmithApp() {
        super("NoteSmith");
        initLookAndFeel();
        initUI();
    }

    private void initLookAndFeel() {
        try {
            // Nimbus looks more modern than default
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) {}
    }

    private void initUI() {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(UIConstants.WINDOW_WIDTH, UIConstants.WINDOW_HEIGHT);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(AppStyles.BG_MAIN);
        
        // Set application icon
        try {
            // Try to load icon from resources
            java.net.URL iconURL = getClass().getResource("/icon.png");
            if (iconURL == null) {
                iconURL = getClass().getResource("/icon.ico");
            }
            if (iconURL != null) {
                Image iconImage = Toolkit.getDefaultToolkit().getImage(iconURL);
                setIconImage(iconImage);
            } else {
                System.err.println("Warning: Icon file not found in resources");
            }
        } catch (Exception e) {
            System.err.println("Warning: Failed to load icon - " + e.getMessage());
        }
        
        // Add shutdown hook to close database connections
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                Database.shutdown();
            }
        });

        loginPanel = new LoginPanel(userRepo, this);
        registerPanel = new RegisterPanel(userRepo, this);

        cardPanel.add(loginPanel, "login");
        cardPanel.add(registerPanel, "register");

        add(cardPanel, BorderLayout.CENTER);

        cardLayout.show(cardPanel, "login");
    }

    @Override
    public void onLoginSuccess(User user) {
        // Each user gets their own file, AND their own DB rows
        String filename = "notes_user_" + user.getId() + ".txt";

        NoteRepository fileRepo = new FileNoteRepository(filename);
        NoteRepository dbRepo   = new JdbcNoteRepository(user.getId());

        // Composite repository: writes to DB + file, reads from DB (fallback: file)
        NoteRepository noteRepo = new DualNoteRepository(fileRepo, dbRepo);

        dashboardPanel = new DashboardPanel(user, noteRepo, this);
        cardPanel.add(dashboardPanel, "dashboard");
        cardLayout.show(cardPanel, "dashboard");
    }

    @Override
    public void onRegisterRequested() {
        cardLayout.show(cardPanel, "register");
    }

    @Override
    public void onBackToLogin() {
        cardLayout.show(cardPanel, "login");
    }

    @Override
    public void onLogout() {
        cardLayout.show(cardPanel, "login");
    }
}
