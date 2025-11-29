package com.notesmith.ui.components;

import com.notesmith.config.AppStyles;

import javax.swing.*;

public class CLabel extends JLabel {

    public CLabel(String text) {
        super(text);
        setForeground(AppStyles.TEXT_PRIMARY);
        setFont(AppStyles.fontNormal());
    }

    public static CLabel title(String text) {
        CLabel label = new CLabel(text);
        label.setFont(AppStyles.fontTitle());
        return label;
    }

    public static CLabel secondary(String text) {
        CLabel label = new CLabel(text);
        label.setForeground(AppStyles.TEXT_SECONDARY);
        label.setFont(AppStyles.fontSmall());
        return label;
    }
}
