package com.serifsystemworks.sofd.ui;

import com.serifsystemworks.sofd.io.TableMapper;
import com.serifsystemworks.sofd.models.*;
import com.serifsystemworks.sofd.models.DropEntry;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class MainFrame extends JFrame {

    private File currentFile;
    private byte[] originalBytes;

    // Loaded Data Caches
    private List<EnemySpec> activeEnemies = new ArrayList<>();
    private List<DropEntry> activeDrops = new ArrayList<>();
    private List<PartyMemberSpec> activeParty = new ArrayList<>();
    private List<EquipmentSpec> activeEquipment = new ArrayList<>();
    private List<PaletteSpec> activePalettes = new ArrayList<>();
    private List<SkillSpec> activeSkills = new ArrayList<>();
    private List<EncounterSpec> activeEncounters = new ArrayList<>();

    // UI Panels
    private EnemyPanel enemyPanel;
    private DropsPanel dropsPanel;
    private PartyPanel partyPanel;
    private EquipmentPanel equipmentPanel;
    private PalettePanel palettePanel;
    private SkillPanel skillPanel;
    private EncounterPanel encounterPanel;

    private JLabel lblStatus;
    private JTextField txtSeed;
    private JComboBox<String> comboPreset;

    public MainFrame() {
        super("[ SOFD_RANDOMIZER_V3.0 ] - EXE/BIN TOOLSET");
        initUI();
    }

    private void initUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1024, 720);
        setLocationRelativeTo(null);
        getContentPane().setBackground(Color.BLACK);
        setLayout(new BorderLayout(5, 5));

        // Color & Font Defaults
        Color neonGreen = new Color(0, 255, 102);
        Font monoFont = new Font("Monospaced", Font.BOLD, 12);

        // --- TOP TOOLBAR PANEL ---
        JPanel pnlToolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        pnlToolbar.setBackground(Color.BLACK);
        pnlToolbar.setBorder(new LineBorder(neonGreen, 1));

        JButton btnLoad = createStyledButton("LOAD BIN", neonGreen, monoFont);
        JButton btnSave = createStyledButton("SAVE BIN", neonGreen, monoFont);
        JButton btnExport = createStyledButton("EXPORT PATCH", neonGreen, monoFont);

        btnLoad.addActionListener(e -> onOpenFile());
        btnSave.addActionListener(e -> onSaveFile());
        btnExport.addActionListener(e -> onExportPatch());

        JLabel lblSeed = new JLabel("SEED:");
        lblSeed.setForeground(neonGreen);
        lblSeed.setFont(monoFont);

        txtSeed = new JTextField("NEXUS_2026", 10);
        txtSeed.setBackground(Color.BLACK);
        txtSeed.setForeground(neonGreen);
        txtSeed.setCaretColor(neonGreen);
        txtSeed.setFont(monoFont);
        txtSeed.setBorder(new LineBorder(neonGreen, 1));

        JLabel lblPreset = new JLabel("PRESET:");
        lblPreset.setForeground(neonGreen);
        lblPreset.setFont(monoFont);

        comboPreset = new JComboBox<>(new String[]{"BALANCED", "CHAOS", "VANILLA", "HARDCORE"});
        comboPreset.setBackground(Color.BLACK);
        comboPreset.setForeground(neonGreen);
        comboPreset.setFont(monoFont);
        comboPreset.setBorder(new LineBorder(neonGreen, 1));

        JButton btnMutate = createStyledButton("MUTATE", neonGreen, monoFont);

        pnlToolbar.add(btnLoad);
        pnlToolbar.add(btnSave);
        pnlToolbar.add(btnExport);
        pnlToolbar.add(Box.createHorizontalStrut(15));
        pnlToolbar.add(lblSeed);
        pnlToolbar.add(txtSeed);
        pnlToolbar.add(lblPreset);
        pnlToolbar.add(comboPreset);
        pnlToolbar.add(btnMutate);

        add(pnlToolbar, BorderLayout.NORTH);

        // --- CENTER TABBED PANE ---
        JTabbedPane tabPane = new JTabbedPane();
        tabPane.setBackground(Color.BLACK);
        tabPane.setForeground(neonGreen);
        tabPane.setFont(monoFont);

        enemyPanel = new EnemyPanel();
        dropsPanel = new DropsPanel();
        partyPanel = new PartyPanel();
        equipmentPanel = new EquipmentPanel();
        palettePanel = new PalettePanel();
        skillPanel = new SkillPanel();
        encounterPanel = new EncounterPanel();

        tabPane.addTab("ENEMIES", enemyPanel);
        tabPane.addTab("DROPS", dropsPanel);
        tabPane.addTab("PARTY", partyPanel);
        tabPane.addTab("EQUIPMENT", equipmentPanel);
        tabPane.addTab("PALETTES", palettePanel);
        tabPane.addTab("SKILLS", skillPanel);
        tabPane.addTab("ENCOUNTERS", encounterPanel);

        // Apply dark neon styling across all embedded panel tables
        applyPhosphorThemeToPanels(neonGreen, monoFont);

        add(tabPane, BorderLayout.CENTER);

        // --- BOTTOM STATUS BAR ---
        JPanel pnlStatus = new JPanel(new BorderLayout());
        pnlStatus.setBackground(Color.BLACK);
        pnlStatus.setBorder(new LineBorder(neonGreen, 1));

        lblStatus = new JLabel(" SYSTEM READY // AWAITING BINARY");
        lblStatus.setForeground(neonGreen);
        lblStatus.setFont(monoFont);
        lblStatus.setPreferredSize(new Dimension(1024, 25));

        pnlStatus.add(lblStatus, BorderLayout.WEST);
        add(pnlStatus, BorderLayout.SOUTH);
    }

    private JButton createStyledButton(String text, Color fg, Font font) {
        JButton btn = new JButton(text);
        btn.setBackground(Color.BLACK);
        btn.setForeground(fg);
        btn.setFont(font);
        btn.setFocusPainted(false);
        btn.setBorder(new LineBorder(fg, 1));
        return btn;
    }

    private void applyPhosphorThemeToPanels(Color neonGreen, Font monoFont) {
        JPanel[] panels = new JPanel[]{
                enemyPanel, dropsPanel, partyPanel,
                equipmentPanel, palettePanel, skillPanel, encounterPanel
        };

        for (JPanel panel : panels) {
            panel.setBackground(Color.BLACK);
            styleChildComponents(panel, neonGreen, monoFont);
        }
    }

    private void styleChildComponents(Container container, Color neonGreen, Font monoFont) {
        for (Component comp : container.getComponents()) {
            if (comp instanceof JTable) {
                JTable table = (JTable) comp;
                table.setBackground(Color.BLACK);
                table.setForeground(neonGreen);
                table.setGridColor(new Color(0, 80, 30));
                table.setFont(monoFont);
                table.setSelectionBackground(new Color(0, 100, 40));
                table.setSelectionForeground(Color.GREEN);

                JTableHeader header = table.getTableHeader();
                header.setBackground(Color.BLACK);
                header.setForeground(neonGreen);
                header.setFont(monoFont);
                header.setBorder(new LineBorder(neonGreen, 1));

                DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
                renderer.setBackground(Color.BLACK);
                renderer.setForeground(neonGreen);
                table.setDefaultRenderer(Object.class, renderer);
            } else if (comp instanceof JScrollPane) {
                JScrollPane scrollPane = (JScrollPane) comp;
                scrollPane.setBackground(Color.BLACK);
                scrollPane.getViewport().setBackground(Color.BLACK);
                scrollPane.setBorder(new LineBorder(neonGreen, 1));
                styleChildComponents(scrollPane, neonGreen, monoFont);
            } else if (comp instanceof Container) {
                comp.setBackground(Color.BLACK);
                styleChildComponents((Container) comp, neonGreen, monoFont);
            }
        }
    }

    private void onOpenFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("SELECT so1pack.bin (PSP_GAME/USRDIR/so1pack.bin) — NOT DATA.BIN, that's the firmware updater");
        int result = chooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            currentFile = chooser.getSelectedFile();
            try {
                originalBytes = Files.readAllBytes(currentFile.toPath());

                activeEnemies = TableMapper.loadEnemies(currentFile);
                enemyPanel.updateTableData(activeEnemies);

                activeDrops = TableMapper.loadDrops(currentFile);
                dropsPanel.updateTableData(activeDrops);

                activeParty = TableMapper.loadParty(currentFile);
                partyPanel.updateTableData(activeParty);

                activeEquipment = TableMapper.loadEquipment(currentFile);
                equipmentPanel.updateTableData(activeEquipment);

                activePalettes = TableMapper.loadPalettes(currentFile);
                palettePanel.updateTableData(activePalettes);

                activeSkills = TableMapper.loadSkills(currentFile);
                skillPanel.updateTableData(activeSkills);

                activeEncounters = TableMapper.loadEncounters(currentFile);
                encounterPanel.updateTableData(activeEncounters);

                lblStatus.setText(" LOADED BINARY TARGET: " + currentFile.getName().toUpperCase());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "ERROR READING BINARY: " + ex.getMessage(), "SYSTEM ERROR", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void onSaveFile() {
        if (currentFile == null) {
            JOptionPane.showMessageDialog(this, "NO TARGET FILE LOADED", "WARNING", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            TableMapper.saveEnemies(currentFile, activeEnemies);
            TableMapper.saveDrops(currentFile, activeDrops);
            TableMapper.saveParty(currentFile, activeParty);
            TableMapper.saveEquipment(currentFile, activeEquipment);
            TableMapper.savePalettes(currentFile, activePalettes);
            TableMapper.saveSkills(currentFile, activeSkills);
            TableMapper.saveEncounters(currentFile, activeEncounters);

            lblStatus.setText(" BINARY MUTATIONS WRITTEN TO: " + currentFile.getName().toUpperCase());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "ERROR WRITING TO BINARY: " + ex.getMessage(), "SYSTEM ERROR", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onExportPatch() {
        if (currentFile == null || originalBytes == null) {
            JOptionPane.showMessageDialog(this, "NO BINARY LOADED IN MEMORY TO GENERATE PATCH FROM", "WARNING", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("SAVE PATCH FILE");
        chooser.setSelectedFile(new File(currentFile.getName() + ".patch"));

        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File patchFile = chooser.getSelectedFile();
            try {
                // Ensure current pending UI state is flushed to disk/memory state before computing diff
                TableMapper.saveEnemies(currentFile, activeEnemies);
                TableMapper.saveDrops(currentFile, activeDrops);
                TableMapper.saveParty(currentFile, activeParty);
                TableMapper.saveEquipment(currentFile, activeEquipment);
                TableMapper.savePalettes(currentFile, activePalettes);
                TableMapper.saveSkills(currentFile, activeSkills);
                TableMapper.saveEncounters(currentFile, activeEncounters);

                byte[] modifiedBytes = Files.readAllBytes(currentFile.toPath());
                int diffCount = 0;

                try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(patchFile))) {
                    dos.writeBytes("SOFDPATCH10"); // Patch header magic bytes
                    int length = Math.min(originalBytes.length, modifiedBytes.length);

                    for (int i = 0; i < length; i++) {
                        if (originalBytes[i] != modifiedBytes[i]) {
                            dos.writeInt(i);
                            dos.writeByte(modifiedBytes[i]);
                            diffCount++;
                        }
                    }
                    dos.flush();
                }

                lblStatus.setText(" PATCH EXPORTED: " + diffCount + " BYTE MUTATIONS WRITTEN TO " + patchFile.getName().toUpperCase());
                JOptionPane.showMessageDialog(this, "Patch created successfully with " + diffCount + " byte mutations.", "EXPORT COMPLETE", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "ERROR EXPORTING PATCH: " + ex.getMessage(), "SYSTEM ERROR", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}