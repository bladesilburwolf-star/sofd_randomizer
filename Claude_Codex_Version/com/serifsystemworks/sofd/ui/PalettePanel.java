package com.serifsystemworks.sofd.ui;

import com.serifsystemworks.sofd.models.PaletteSpec;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class PalettePanel extends JPanel {
    private JTable table;
    private DefaultTableModel tableModel;

    public PalettePanel() {
        setLayout(new BorderLayout());
        String[] columns = {"Sprite ID", "CLUT Offset", "Palette Name", "Colors"};
        tableModel = new DefaultTableModel(columns, 0);
        table = new JTable(tableModel);
        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    public void updateTableData(List<PaletteSpec> palettes) {
        tableModel.setRowCount(0);
        if (palettes == null) return;
        for (PaletteSpec spec : palettes) {
            tableModel.addRow(new Object[]{
                    String.format("0x%04X", spec.getSpriteId()),
                    String.format("0x%08X", spec.getClutOffset()),
                    spec.getPaletteName(),
                    spec.getArgbColors().length + " colors"
            });
        }
    }

    public void updateData(List<PaletteSpec> palettes) {
        updateTableData(palettes);
    }
}