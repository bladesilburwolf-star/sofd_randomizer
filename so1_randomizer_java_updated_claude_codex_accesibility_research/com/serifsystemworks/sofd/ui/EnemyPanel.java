package com.serifsystemworks.sofd.ui;

import com.serifsystemworks.sofd.models.EnemySpec;
import com.serifsystemworks.sofd.randomizer.EnemyRandomizer;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class EnemyPanel extends JPanel {

    private JCheckBox chkStats;
    private JCheckBox chkRewards;
    private JCheckBox chkResists;
    private JSpinner spinMinScale;
    private JSpinner spinMaxScale;
    private JTextField txtSeed;
    private JTable enemyTable;
    private DefaultTableModel tableModel;

    private List<EnemySpec> loadedEnemies = new ArrayList<>();

    public EnemyPanel() {
        setBackground(ThemeManager.COLOR_PANEL_BG);
        setLayout(new BorderLayout(10, 10));
        initControls();
        initTable();
    }

    private void initControls() {
        JPanel optionsPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        optionsPanel.setBackground(ThemeManager.COLOR_PANEL_BG);
        
        TitledBorder border1 = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(ThemeManager.COLOR_PHOSPHOR, 1), 
                "Stat Randomization Options"
        );
        border1.setTitleColor(ThemeManager.COLOR_PHOSPHOR);
        border1.setTitleFont(ThemeManager.EGA_FONT);
        optionsPanel.setBorder(border1);

        chkStats = new JCheckBox("Randomize Enemy Stats (HP/ATK/DEF/AGL)", true);
        chkRewards = new JCheckBox("Randomize Rewards (EXP/Fol)", true);
        chkResists = new JCheckBox("Shuffle Elemental Resistances", false);

        JPanel checkGroup = new JPanel(new FlowLayout(FlowLayout.LEFT));
        checkGroup.setBackground(ThemeManager.COLOR_PANEL_BG);
        checkGroup.add(chkStats);
        checkGroup.add(chkRewards);
        checkGroup.add(chkResists);

        JPanel scaleGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        scaleGroup.setBackground(ThemeManager.COLOR_PANEL_BG);

        spinMinScale = new JSpinner(new SpinnerNumberModel(0.8, 0.1, 5.0, 0.1));
        spinMaxScale = new JSpinner(new SpinnerNumberModel(1.3, 0.1, 5.0, 0.1));
        
        // Custom styling spinner input text
        ((JSpinner.DefaultEditor) spinMinScale.getEditor()).getTextField().setForeground(ThemeManager.COLOR_PHOSPHOR);
        ((JSpinner.DefaultEditor) spinMinScale.getEditor()).getTextField().setBackground(ThemeManager.COLOR_PANEL_BG);
        ((JSpinner.DefaultEditor) spinMaxScale.getEditor()).getTextField().setForeground(ThemeManager.COLOR_PHOSPHOR);
        ((JSpinner.DefaultEditor) spinMaxScale.getEditor()).getTextField().setBackground(ThemeManager.COLOR_PANEL_BG);

        txtSeed = new JTextField(String.valueOf(Math.abs(new Random().nextLong())), 10);
        JButton btnNewSeed = new JButton("New Seed");

        btnNewSeed.addActionListener(e -> txtSeed.setText(String.valueOf(Math.abs(new Random().nextLong()))));

        JLabel lblMin = new JLabel("Min Scale:");
        JLabel lblMax = new JLabel("Max Scale:");
        JLabel lblSeed = new JLabel("Seed:");

        scaleGroup.add(lblMin);
        scaleGroup.add(spinMinScale);
        scaleGroup.add(lblMax);
        scaleGroup.add(spinMaxScale);
        scaleGroup.add(lblSeed);
        scaleGroup.add(txtSeed);
        scaleGroup.add(btnNewSeed);

        optionsPanel.add(checkGroup);
        optionsPanel.add(scaleGroup);

        add(optionsPanel, BorderLayout.NORTH);
    }

    private void initTable() {
        String[] columns = {"ID", "Name", "HP", "MP", "ATK", "DEF", "AGL", "EXP", "Fol", "Resist Mask"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        enemyTable = new JTable(tableModel);
        enemyTable.setBackground(ThemeManager.COLOR_PANEL_BG);
        enemyTable.setForeground(ThemeManager.COLOR_PHOSPHOR);
        
        JScrollPane scrollPane = new JScrollPane(enemyTable);
        scrollPane.getViewport().setBackground(ThemeManager.COLOR_PANEL_BG);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setBackground(ThemeManager.COLOR_PANEL_BG);
        
        JButton btnApply = new JButton("Apply Enemy Randomizer");
        btnApply.addActionListener(this::onApplyRandomizer);
        bottomPanel.add(btnApply);

        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void onApplyRandomizer(ActionEvent e) {
        if (loadedEnemies == null || loadedEnemies.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No enemy data loaded to randomize.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            long seed = Long.parseLong(txtSeed.getText().trim());
            EnemyRandomizer randomizer = new EnemyRandomizer(seed);

            randomizer.setRandomizeStats(chkStats.isSelected());
            randomizer.setRandomizeRewards(chkRewards.isSelected());
            randomizer.setShuffleResistances(chkResists.isSelected());
            randomizer.setScaleMin(((Number) spinMinScale.getValue()).doubleValue());
            randomizer.setScaleMax(((Number) spinMaxScale.getValue()).doubleValue());

            randomizer.processEnemies(loadedEnemies);
            updateTableData(loadedEnemies);

            JOptionPane.showMessageDialog(this, "Enemy stats randomized successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid seed format.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void updateTableData(List<EnemySpec> enemies) {
        this.loadedEnemies = enemies;
        tableModel.setRowCount(0);

        for (EnemySpec enemy : enemies) {
            tableModel.addRow(new Object[]{
                    String.format("0x%03X", enemy.getId()),
                    enemy.getName(),
                    enemy.getHp(),
                    enemy.getMp(),
                    enemy.getAttack(),
                    enemy.getDefense(),
                    enemy.getAgility(),
                    enemy.getExpReward(),
                    enemy.getFolReward(),
                    String.format("0x%02X", enemy.getElementResistMask())
            });
        }
    }
}