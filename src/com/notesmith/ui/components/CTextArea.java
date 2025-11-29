package com.notesmith.ui.components;

import com.notesmith.config.AppStyles;

import javax.swing.*;

public class CTextArea extends JTextArea {
    public CTextArea(int rows, int columns) {
        super(rows, columns);
        setLineWrap(true);
        setWrapStyleWord(true);
        setBackground(AppStyles.BG_CARD);
        setForeground(AppStyles.TEXT_PRIMARY);
        setCaretColor(AppStyles.TEXT_PRIMARY);
        setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
        setFont(AppStyles.fontNormal());
    }
}
