package com.notesmith;

import com.notesmith.ui.NoteSmithApp;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new NoteSmithApp().setVisible(true));
    }
}
