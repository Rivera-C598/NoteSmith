package com.notesmith.ui.components;

import com.notesmith.config.AppStyles;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class CButton extends JButton {
    
    private Color normalColor;
    private Color hoverColor;

    public CButton(String text) {
        super(text);
        this.normalColor = AppStyles.ACCENT;
        this.hoverColor = brighten(AppStyles.ACCENT);
        
        setFocusPainted(false);
        setForeground(AppStyles.TEXT_PRIMARY);
        setBackground(normalColor);
        setFont(AppStyles.fontBold());
        setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setOpaque(true);
        setBorderPainted(false);
        
        // Add hover effect
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                setBackground(hoverColor);
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                setBackground(normalColor);
            }
        });
    }

    public static CButton danger(String text) {
        CButton btn = new CButton(text);
        btn.normalColor = AppStyles.ACCENT_DANGER;
        btn.hoverColor = brighten(AppStyles.ACCENT_DANGER);
        btn.setBackground(btn.normalColor);
        return btn;
    }
    
    public static CButton secondary(String text) {
        CButton btn = new CButton(text);
        btn.normalColor = AppStyles.BG_HOVER;
        btn.hoverColor = brighten(AppStyles.BG_HOVER);
        btn.setBackground(btn.normalColor);
        return btn;
    }
    
    public static CButton ai(String text) {
        CButton btn = new CButton(text);
        btn.normalColor = AppStyles.AI_ACCENT;
        btn.hoverColor = brighten(AppStyles.AI_ACCENT);
        btn.setBackground(btn.normalColor);
        return btn;
    }
    
    private static Color brighten(Color color) {
        int r = Math.min(255, color.getRed() + 30);
        int g = Math.min(255, color.getGreen() + 30);
        int b = Math.min(255, color.getBlue() + 30);
        return new Color(r, g, b);
    }
}
