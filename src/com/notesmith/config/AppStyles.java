package com.notesmith.config;

import java.awt.*;

public final class AppStyles {

    private AppStyles() {}

    // Colors
    public static final Color BG_MAIN = new Color(0x121212);
    public static final Color BG_CARD = new Color(0x1E1E1E);
    public static final Color ACCENT = new Color(0x4CAF50);
    public static final Color ACCENT_DANGER = new Color(0xE53935);
    public static final Color TEXT_PRIMARY = new Color(0xFFFFFF);
    public static final Color TEXT_SECONDARY = new Color(0xB0BEC5);

    // Fonts (change here)
    public static final String BASE_FONT_FAMILY = "Segoe UI";
    public static final int FONT_SIZE_NORMAL = 14;
    public static final int FONT_SIZE_TITLE = 20;

    public static Font fontNormal() {
        return new Font(BASE_FONT_FAMILY, Font.PLAIN, FONT_SIZE_NORMAL);
    }

    public static Font fontTitle() {
        return new Font(BASE_FONT_FAMILY, Font.BOLD, FONT_SIZE_TITLE);
    }

    public static Font fontSmall() {
        return new Font(BASE_FONT_FAMILY, Font.PLAIN, 12);
    }
}
