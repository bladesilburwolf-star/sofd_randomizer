package com.serifsystemworks.sofd.ui;

import com.serifsystemworks.sofd.models.DropEntry;
import com.serifsystemworks.sofd.randomizer.DropTableEngine;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class DropsPanel extends JPanel {
    private JTable table;
    private DefaultTableModel tableModel;
    private JSpinner spinSeed;
    private JSpinner spinMaxItemId;
    private JButton btnRandomize;
    private List<DropEntry> loadedDrops = new ArrayList<>();

    public DropsPanel() {
        setLayout(new BorderLayout());

        // Control bar top
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        controlPanel.add(new JLabel("Seed:"));
        spinSeed = new JSpinner(new SpinnerNumberModel(12345L, 0L, Long.MAX_VALUE, 1L));
        controlPanel.add(spinSeed);

        controlPanel.add(new JLabel("Max Item ID:"));
        spinMaxItemId = new JSpinner(new SpinnerNumberModel(255, 1, 65535, 1));
        controlPanel.add(spinMaxItemId);

        btnRandomize = new JButton("Randomize Drops");
        controlPanel.add(btnRandomize);

        add(controlPanel, BorderLayout.NORTH);

        // Table setup
        String[] columns = {"ID", "Enemy ID", "Item ID", "Drop Rate (%)", "Flags"};
        tableModel = new DefaultTableModel(columns, 0);
        table = new JTable(tableModel);
        add(new JScrollPane(table), BorderLayout.CENTER);

        btnRandomize.addActionListener(e -> randomizeData());
    }

    private void randomizeData() {
        if (loadedDrops == null || loadedDrops.isEmpty()) return;

        try {
            long seed = ((Number) spinSeed.getValue()).longValue();
            DropTableEngine engine = new DropTableEngine(seed);
            engine.setMaxItemId(((Number) spinMaxItemId.getValue()).intValue());
            engine.randomize(loadedDrops);

            updateTableData(loadedDrops);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error during randomization: " + ex.getMessage(),
                    "Randomization Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void updateTableData(List<DropEntry> drops) {
        this.loadedDrops = drops;
        tableModel.setRowCount(0);
        if (drops == null) return;

        for (DropEntry drop : drops) {
            tableModel.addRow(new Object[]{
                    String.format("0x%04X", drop.getId()),
                    String.format("0x%04X", drop.getEnemyId()),
                    String.format("0x%04X", drop.getItemId()),
                    drop.getDropRate(),
                    String.format("0x%04X", drop.getExtraFlags())
            });
        }
    }

    public void updateData(List<DropEntry> drops) {
        updateTableData(drops);
    }

    public List<DropEntry> getLoadedDrops() {
        return loadedDrops;
    }
}