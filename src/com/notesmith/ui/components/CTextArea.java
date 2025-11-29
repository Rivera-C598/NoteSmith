package com.notesmith.ui.components;

import com.notesmith.config.AppStyles;

import javax.swing.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

public class CTextArea extends JTextArea {
    
    public CTextArea(int rows, int columns) {
        super(rows, columns);
        setBackground(AppStyles.BG_CARD);
        setForeground(AppStyles.TEXT_PRIMARY);
        setCaretColor(AppStyles.ACCENT);
        setFont(AppStyles.fontNormal());
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(AppStyles.BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        setLineWrap(true);
        setWrapStyleWord(true);
        setSelectionColor(AppStyles.ACCENT);
        setSelectedTextColor(AppStyles.TEXT_PRIMARY);
        
        // Add focus effect
        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(AppStyles.ACCENT, 2),
                    BorderFactory.createEmptyBorder(9, 11, 9, 11)
                ));
            }
            
            @Override
            public void focusLost(FocusEvent e) {
                setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(AppStyles.BORDER_COLOR, 1),
                    BorderFactory.createEmptyBorder(10, 12, 10, 12)
                ));
            }
        });
    }
}
