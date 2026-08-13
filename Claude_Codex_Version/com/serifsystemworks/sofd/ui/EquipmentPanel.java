package com.serifsystemworks.sofd.ui;

import com.serifsystemworks.sofd.models.EquipmentSpec;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class EquipmentPanel extends JPanel {
    private JTable table;
    private DefaultTableModel tableModel;

    public EquipmentPanel() {
        setLayout(new BorderLayout());
        String[] columns = {"ID", "Type", "Name", "ATK", "DEF", "MAG", "HIT", "AVD", "Price"};
        tableModel = new DefaultTableModel(columns, 0);
        table = new JTable(tableModel);
        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    public void updateTableData(List<EquipmentSpec> equipment) {
        tableModel.setRowCount(0);
        if (equipment == null) return;
        for (EquipmentSpec item : equipment) {
            tableModel.addRow(new Object[]{
                    String.format("0x%04X", item.getId()),
                    item.getTypeName(),
                    item.getName(),
                    item.getAtk(),
                    item.getDef(),
                    item.getMagAtk(),
                    item.getHit(),
                    item.getAvd(),
                    item.getPrice()
            });
        }
    }

    public void updateData(List<EquipmentSpec> equipment) {
        updateTableData(equipment);
    }
}