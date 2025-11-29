package com.notesmith.config;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

public final class AppStyles {

    private AppStyles() {}

    // Modern Color Palette
    public static final Color BG_MAIN = new Color(0x0F0F0F);           // Darker main background
    public static final Color BG_CARD = new Color(0x1A1A1A);           // Card background
    public static final Color BG_HOVER = new Color(0x252525);          // Hover state
    public static final Color ACCENT = new Color(0x00D9FF);            // Cyan accent
    public static final Color ACCENT_SECONDARY = new Color(0x7C3AED);  // Purple accent
    public static final Color ACCENT_DANGER = new Color(0xFF4757);     // Red
    public static final Color ACCENT_SUCCESS = new Color(0x00E676);    // Green
    public static final Color TEXT_PRIMARY = new Color(0xFFFFFF);
    public static final Color TEXT_SECONDARY = new Color(0x9CA3AF);
    public static final Color BORDER_COLOR = new Color(0x2A2A2A);
    
    // AI-specific colors
    public static final Color AI_ACCENT = new Color(0xA78BFA);         // Light purple for AI
    public static final Color AI_BG = new Color(0x1F1B2E);             // Dark purple background

    // Fonts
    public static final String BASE_FONT_FAMILY = "Segoe UI";
    public static final int FONT_SIZE_NORMAL = 14;
    public static final int FONT_SIZE_TITLE = 22;
    public static final int FONT_SIZE_SUBTITLE = 16;

    public static Font fontNormal() {
        return new Font(BASE_FONT_FAMILY, Font.PLAIN, FONT_SIZE_NORMAL);
    }

    public static Font fontTitle() {
        return new Font(BASE_FONT_FAMILY, Font.BOLD, FONT_SIZE_TITLE);
    }
    
    public static Font fontSubtitle() {
        return new Font(BASE_FONT_FAMILY, Font.BOLD, FONT_SIZE_SUBTITLE);
    }

    public static Font fontSmall() {
        return new Font(BASE_FONT_FAMILY, Font.PLAIN, 12);
    }
    
    public static Font fontBold() {
        return new Font(BASE_FONT_FAMILY, Font.BOLD, FONT_SIZE_NORMAL);
    }
    
    // Borders
    public static Border createRoundedBorder(int radius) {
        return BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        );
    }
    
    public static Border createCardBorder() {
        return BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(16, 16, 16, 16)
        );
    }
    
    public static Border createSectionBorder() {
        return BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR),
            BorderFactory.createEmptyBorder(12, 0, 12, 0)
        );
    }
}
