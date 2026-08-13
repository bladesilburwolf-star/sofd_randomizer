package com.serifsystemworks.sofd.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/** A friendly front end for BinaryResearchTool. Every action is read-only. */
public class ResearchPanel extends JPanel {
    private final JTextField file = new JTextField(36);
    private final JComboBox<String> mode = new JComboBox<String>(new String[]{"Find text", "Inspect offset", "Find 16-bit ID", "Find ID runs", "Validate named records", "Locate unaligned SLZ", "Extract SLZ"});
    private final JTextField value = new JTextField("Muah Castle", 16);
    private final JTextField second = new JTextField("0x3EA40", 12);
    private final JTextField third = new JTextField("0x100", 12);
    private final JSpinner ram = new JSpinner(new SpinnerNumberModel(1024, 256, 8192, 256));
    private final JTextArea output = new JTextArea();

    public ResearchPanel() {
        setLayout(new BorderLayout(12, 12));
        setBorder(new EmptyBorder(16, 16, 16, 16));
        JPanel controls = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 5, 5); c.anchor = GridBagConstraints.WEST;
        addRow(controls, c, 0, "Binary or decompressed file", file);
        JButton choose = new JButton("Browse...");
        choose.addActionListener(e -> chooseFile()); c.gridx = 2; c.gridy = 0; controls.add(choose, c);
        addRow(controls, c, 1, "Research action", mode);
        addRow(controls, c, 2, "Value / search text", value);
        addRow(controls, c, 3, "Offset / secondary value", second);
        addRow(controls, c, 4, "Length / third value", third);
        addRow(controls, c, 5, "Helper memory limit (MB)", ram);
        JLabel hint = new JLabel("Read-only: this tab never modifies a game file. Values accept decimal or 0x hexadecimal.");
        c.gridx = 0; c.gridy = 6; c.gridwidth = 3; controls.add(hint, c);
        JButton run = new JButton("Run research tool");
        run.addActionListener(e -> runTool()); c.gridy = 7; controls.add(run, c);
        add(controls, BorderLayout.NORTH);
        mode.addActionListener(e -> setSuggestedInputs());

        output.setEditable(false); output.setLineWrap(false);
        output.setText("Choose a file and an action. Results will appear here.\n\nExamples:\n"
                + "• Find text: Muah Castle\n• Inspect offset: value 0x3EA40, length 0x100\n"
                + "• Find ID runs: maximum ID 0x200, minimum run 6\n"
                + "• Validate named records: stride 0x60, name offset 0x4C, name length 0x14\n");
        add(new JScrollPane(output), BorderLayout.CENTER);
    }

    private void addRow(JPanel panel, GridBagConstraints c, int row, String label, JComponent control) {
        c.gridx = 0; c.gridy = row; c.gridwidth = 1; panel.add(new JLabel(label), c);
        c.gridx = 1; c.fill = GridBagConstraints.HORIZONTAL; c.weightx = 1; panel.add(control, c); c.fill = GridBagConstraints.NONE; c.weightx = 0;
    }

    private void chooseFile() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) file.setText(chooser.getSelectedFile().getAbsolutePath());
    }

    private void runTool() {
        if (file.getText().trim().isEmpty()) { JOptionPane.showMessageDialog(this, "Choose a binary or decompressed file first."); return; }
        final List<String> command = new ArrayList<String>();
        command.add(new File(System.getProperty("java.home"), "bin/java").getPath());
        command.add("-Xmx" + ram.getValue() + "m");
        command.add("-cp"); command.add(System.getProperty("java.class.path"));
        String selected = (String) mode.getSelectedItem();
        boolean slz = selected.equals("Locate unaligned SLZ") || selected.equals("Extract SLZ");
        command.add(slz ? "com.serifsystemworks.sofd.tools.SlzResearchTool" : "com.serifsystemworks.sofd.tools.BinaryResearchTool");
        String target = file.getText().trim(); command.add(selected.equals("Find text") ? "strings" : selected.equals("Inspect offset") ? "dump" : selected.equals("Find 16-bit ID") ? "refs16" : selected.equals("Find ID runs") ? "runs16" : selected.equals("Validate named records") ? "names" : selected.equals("Locate unaligned SLZ") ? "locate" : "extract");
        command.add(target);
        command.add(value.getText().trim());
        if (!selected.equals("Find text") && !second.getText().trim().isEmpty()) command.add(second.getText().trim());
        if ((selected.equals("Inspect offset") || selected.equals("Find ID runs") || selected.equals("Validate named records")) && !third.getText().trim().isEmpty()) command.add(third.getText().trim());
        output.setText("Running read-only analysis with " + ram.getValue() + " MB available...\n");
        new SwingWorker<String, Void>() {
            protected String doInBackground() throws Exception {
                Process p = new ProcessBuilder(command).redirectErrorStream(true).start();
                BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
                StringBuilder text = new StringBuilder(); String line;
                while ((line = reader.readLine()) != null) text.append(line).append('\n');
                int exit = p.waitFor();
                return text.append("\nProcess finished (exit ").append(exit).append(").").toString();
            }
            protected void done() { try { output.setText(get()); output.setCaretPosition(0); } catch (Exception ex) { output.setText("Could not run analysis: " + ex.getMessage()); } }
        }.execute();
    }

    private void setSuggestedInputs() {
        String selected = (String) mode.getSelectedItem();
        if (selected.equals("Find text")) { value.setText("Muah Castle"); second.setText(""); third.setText(""); }
        else if (selected.equals("Inspect offset")) { value.setText("0x3EA40"); second.setText("0x100"); third.setText(""); }
        else if (selected.equals("Find 16-bit ID")) { value.setText("0x0018"); second.setText(""); third.setText(""); }
        else if (selected.equals("Find ID runs")) { value.setText("0x0200"); second.setText("6"); third.setText(""); }
        else if (selected.equals("Validate named records")) { value.setText("0x60"); second.setText("0x4C"); third.setText("0x14"); }
        else if (selected.equals("Locate unaligned SLZ")) { value.setText("0x1BA0000"); second.setText("0x1BB0000"); third.setText(""); }
        else { value.setText("0x1BA08F8"); second.setText(""); third.setText(""); }
    }
}
