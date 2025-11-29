package com.notesmith.ui.components;

import com.notesmith.config.AppStyles;

import javax.swing.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

public class CTextField extends JTextField {
    
    public CTextField(int columns) {
        super(columns);
        setBackground(AppStyles.BG_CARD);
        setForeground(AppStyles.TEXT_PRIMARY);
        setCaretColor(AppStyles.ACCENT);
        setFont(AppStyles.fontNormal());
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(AppStyles.BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        setSelectionColor(AppStyles.ACCENT);
        setSelectedTextColor(AppStyles.TEXT_PRIMARY);
        
        // Add focus effect
        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(AppStyles.ACCENT, 2),
                    BorderFactory.createEmptyBorder(7, 11, 7, 11)
                ));
            }
            
            @Override
            public void focusLost(FocusEvent e) {
                setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(AppStyles.BORDER_COLOR, 1),
                    BorderFactory.createEmptyBorder(8, 12, 8, 12)
                ));
            }
        });
    }
}
