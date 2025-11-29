package com.notesmith.ui.components;

import com.notesmith.config.AppStyles;

import javax.swing.*;
import java.awt.*;

public class CButton extends JButton {

    public CButton(String text) {
        super(text);
        setFocusPainted(false);
        setForeground(AppStyles.TEXT_PRIMARY);
        setBackground(AppStyles.ACCENT);
        setFont(AppStyles.fontNormal());
        setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    public static CButton danger(String text) {
        CButton btn = new CButton(text);
        btn.setBackground(AppStyles.ACCENT_DANGER);
        return btn;
    }
}
