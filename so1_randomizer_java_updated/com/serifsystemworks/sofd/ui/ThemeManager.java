package com.serifsystemworks.sofd.ui;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;

public class ThemeManager {

    public static final Color COLOR_BG_TOP = new Color(0x10, 0x12, 0x16);
    public static final Color COLOR_BG_BOTTOM = new Color(0x1E, 0x22, 0x2A);
    public static final Color COLOR_PHOSPHOR = new Color(0x00, 0xFF, 0x66);
    public static final Color COLOR_PANEL_BG = new Color(0x0A, 0x0C, 0x0E);
    public static final Color COLOR_DARK_STEEL = new Color(0x22, 0x26, 0x2E);

    public static final Font EGA_FONT = new Font("Monospaced", Font.BOLD, 14);

    public static void applyTheme() {
        // Core Panels & Windows
        UIManager.put("Panel.background", COLOR_PANEL_BG);
        UIManager.put("Viewport.background", COLOR_PANEL_BG);
        UIManager.put("ScrollPane.background", COLOR_PANEL_BG);
        UIManager.put("ScrollPane.border", new LineBorder(COLOR_PHOSPHOR, 1));

        // Tabs
        UIManager.put("TabbedPane.background", COLOR_DARK_STEEL);
        UIManager.put("TabbedPane.foreground", COLOR_PHOSPHOR);
        UIManager.put("TabbedPane.font", EGA_FONT);
        UIManager.put("TabbedPane.selected", COLOR_BG_BOTTOM);

        // Buttons
        UIManager.put("Button.background", COLOR_DARK_STEEL);
        UIManager.put("Button.foreground", COLOR_PHOSPHOR);
        UIManager.put("Button.font", EGA_FONT);
        UIManager.put("Button.border", new LineBorder(COLOR_PHOSPHOR, 1));

        // Labels & Checkboxes
        UIManager.put("Label.foreground", COLOR_PHOSPHOR);
        UIManager.put("Label.font", EGA_FONT);
        UIManager.put("CheckBox.background", COLOR_PANEL_BG);
        UIManager.put("CheckBox.foreground", COLOR_PHOSPHOR);
        UIManager.put("CheckBox.font", EGA_FONT);

        // Text Fields
        UIManager.put("TextField.background", COLOR_PANEL_BG);
        UIManager.put("TextField.foreground", COLOR_PHOSPHOR);
        UIManager.put("TextField.caretForeground", COLOR_PHOSPHOR);
        UIManager.put("TextField.font", EGA_FONT);
        UIManager.put("TextField.border", new LineBorder(COLOR_PHOSPHOR, 1));

        // Spinners
        UIManager.put("Spinner.background", COLOR_PANEL_BG);
        UIManager.put("Spinner.foreground", COLOR_PHOSPHOR);
        UIManager.put("Spinner.font", EGA_FONT);
        UIManager.put("Spinner.border", new LineBorder(COLOR_PHOSPHOR, 1));
        UIManager.put("FormattedTextField.background", COLOR_PANEL_BG);
        UIManager.put("FormattedTextField.foreground", COLOR_PHOSPHOR);

        // Tables
        UIManager.put("Table.background", COLOR_PANEL_BG);
        UIManager.put("Table.foreground", COLOR_PHOSPHOR);
        UIManager.put("Table.gridColor", COLOR_PHOSPHOR);
        UIManager.put("Table.font", EGA_FONT);
        UIManager.put("Table.rowHeight", 22);
        UIManager.put("TableHeader.background", COLOR_DARK_STEEL);
        UIManager.put("TableHeader.foreground", COLOR_PHOSPHOR);
        UIManager.put("TableHeader.font", EGA_FONT);
        UIManager.put("TableHeader.cellBorder", new LineBorder(COLOR_PHOSPHOR, 1));
    }

    public static JPanel createSteelGradientPanel() {
        return new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, COLOR_BG_TOP, 0, getHeight(), COLOR_BG_BOTTOM);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
    }
}