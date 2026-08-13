package com.serifsystemworks.sofd.ui;

import com.serifsystemworks.sofd.models.EncounterSpec;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Arrays;
import java.util.List;

public class EncounterPanel extends JPanel {
    private JTable table;
    private DefaultTableModel tableModel;

    public EncounterPanel() {
        setLayout(new BorderLayout());
        String[] columns = {"Zone ID", "Zone Name", "Encounter Rate", "Enemy Slots"};
        tableModel = new DefaultTableModel(columns, 0);
        table = new JTable(tableModel);
        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    public void updateTableData(List<EncounterSpec> encounters) {
        tableModel.setRowCount(0);
        if (encounters == null) return;
        for (EncounterSpec zone : encounters) {
            tableModel.addRow(new Object[]{
                    String.format("0x%04X", zone.getZoneId()),
                    zone.getZoneName(),
                    zone.getEncounterRate(),
                    Arrays.toString(zone.getEnemySlotIds())
            });
        }
    }

    public void updateData(List<EncounterSpec> encounters) {
        updateTableData(encounters);
    }
}