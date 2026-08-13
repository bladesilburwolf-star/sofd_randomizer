package com.serifsystemworks.sofd.ui;

import com.serifsystemworks.sofd.models.PartyMemberSpec;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class PartyPanel extends JPanel {
    private JTable table;
    private DefaultTableModel tableModel;

    public PartyPanel() {
        setLayout(new BorderLayout());
        String[] columns = {"Char ID", "Name", "Level", "HP", "MP", "STR", "CON", "AGL", "INT", "LUC", "Weapon ID"};
        tableModel = new DefaultTableModel(columns, 0);
        table = new JTable(tableModel);
        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    public void updateTableData(List<PartyMemberSpec> party) {
        tableModel.setRowCount(0);
        if (party == null) return;
        for (PartyMemberSpec member : party) {
            tableModel.addRow(new Object[]{
                    String.format("0x%02X", member.getCharacterId()),
                    member.getName(),
                    member.getLevel(),
                    member.getHp(),
                    member.getMp(),
                    member.getStr(),
                    member.getCon(),
                    member.getAgl(),
                    member.getIntStat(),
                    member.getLuc(),
                    String.format("0x%04X", member.getInitialWeaponId())
            });
        }
    }

    public void updateData(List<PartyMemberSpec> party) {
        updateTableData(party);
    }
}