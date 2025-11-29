package com.notesmith.ui.components;

import com.notesmith.config.AppStyles;

import javax.swing.*;

public class CTextField extends JTextField {
    public CTextField(int columns) {
        super(columns);
        setBackground(AppStyles.BG_CARD);
        setForeground(AppStyles.TEXT_PRIMARY);
        setCaretColor(AppStyles.TEXT_PRIMARY);
        setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
        setFont(AppStyles.fontNormal());
    }
}
