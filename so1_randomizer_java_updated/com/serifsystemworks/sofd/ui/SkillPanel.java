package com.serifsystemworks.sofd.ui;

import com.serifsystemworks.sofd.models.SkillSpec;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class SkillPanel extends JPanel {
    private JTable table;
    private DefaultTableModel tableModel;

    public SkillPanel() {
        setLayout(new BorderLayout());
        String[] columns = {"ID", "Category", "Skill Name", "Req SP", "Max Level", "IC Success Rate"};
        tableModel = new DefaultTableModel(columns, 0);
        table = new JTable(tableModel);
        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    public void updateTableData(List<SkillSpec> skills) {
        tableModel.setRowCount(0);
        if (skills == null) return;
        for (SkillSpec skill : skills) {
            tableModel.addRow(new Object[]{
                    String.format("0x%04X", skill.getId()),
                    skill.getCategoryName(),
                    skill.getName(),
                    skill.getReqSp(),
                    skill.getMaxLevel(),
                    skill.getIcSuccessRate() + "%"
            });
        }
    }

    public void updateData(List<SkillSpec> skills) {
        updateTableData(skills);
    }
}