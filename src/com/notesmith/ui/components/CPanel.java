package com.notesmith.ui.components;

import com.notesmith.config.AppStyles;

import javax.swing.*;
import java.awt.*;

public class CPanel extends JPanel {
    public CPanel() {
        setBackground(AppStyles.BG_CARD);
    }

    @Override
    public void setLayout(LayoutManager mgr) {
        super.setLayout(mgr);
    }
}
