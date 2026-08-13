package com.serifsystemworks.sofd.ui;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;

/** High-contrast, large-text palette inspired by Minecraft's grass, stone, and wood UI. */
public final class ThemeManager {
    public static final Color COLOR_BG_TOP = new Color(0x1B, 0x24, 0x18);
    public static final Color COLOR_BG_BOTTOM = new Color(0x0E, 0x12, 0x0D);
    public static final Color COLOR_PHOSPHOR = new Color(0xF4, 0xF1, 0xD0);
    public static final Color COLOR_PANEL_BG = new Color(0x24, 0x2C, 0x21);
    public static final Color COLOR_DARK_STEEL = new Color(0x4B, 0x37, 0x24);
    public static final Color COLOR_GRASS = new Color(0x6C, 0xA8, 0x3A);
    private static int fontSize = 18;
    private static boolean highContrast = true;
    public static Font EGA_FONT = font();

    private ThemeManager() { }
    public static Font font() { return new Font("Dialog", Font.BOLD, fontSize); }
    public static void setFontSize(int size) { fontSize = Math.max(14, Math.min(32, size)); EGA_FONT = font(); }
    public static boolean isHighContrast() { return highContrast; }
    public static void setHighContrast(boolean enabled) { highContrast = enabled; }

    public static void applyTheme() {
        Color foreground = highContrast ? COLOR_PHOSPHOR : new Color(0xD7, 0xE6, 0xC9);
        Color panel = highContrast ? COLOR_PANEL_BG : new Color(0x2B, 0x32, 0x29);
        Font f = font();
        UIManager.put("Panel.background", panel); UIManager.put("Viewport.background", panel);
        UIManager.put("ScrollPane.background", panel); UIManager.put("ScrollPane.border", new LineBorder(COLOR_GRASS, 2));
        UIManager.put("TabbedPane.background", COLOR_DARK_STEEL); UIManager.put("TabbedPane.foreground", foreground); UIManager.put("TabbedPane.font", f); UIManager.put("TabbedPane.selected", COLOR_GRASS);
        UIManager.put("Button.background", COLOR_DARK_STEEL); UIManager.put("Button.foreground", foreground); UIManager.put("Button.font", f); UIManager.put("Button.border", new LineBorder(COLOR_GRASS, 2));
        UIManager.put("Label.foreground", foreground); UIManager.put("Label.font", f); UIManager.put("CheckBox.background", panel); UIManager.put("CheckBox.foreground", foreground); UIManager.put("CheckBox.font", f);
        UIManager.put("TextField.background", new Color(0x12, 0x16, 0x11)); UIManager.put("TextField.foreground", foreground); UIManager.put("TextField.caretForeground", foreground); UIManager.put("TextField.font", f); UIManager.put("TextField.border", new LineBorder(COLOR_GRASS, 2));
        UIManager.put("TextArea.background", new Color(0x12, 0x16, 0x11)); UIManager.put("TextArea.foreground", foreground); UIManager.put("TextArea.font", new Font("Monospaced", Font.PLAIN, fontSize));
        UIManager.put("Spinner.font", f); UIManager.put("FormattedTextField.font", f);
        UIManager.put("Table.background", panel); UIManager.put("Table.foreground", foreground); UIManager.put("Table.gridColor", COLOR_GRASS); UIManager.put("Table.font", f); UIManager.put("Table.rowHeight", fontSize + 14);
        UIManager.put("TableHeader.background", COLOR_DARK_STEEL); UIManager.put("TableHeader.foreground", foreground); UIManager.put("TableHeader.font", f); UIManager.put("TableHeader.cellBorder", new LineBorder(COLOR_GRASS, 1));
    }
}
