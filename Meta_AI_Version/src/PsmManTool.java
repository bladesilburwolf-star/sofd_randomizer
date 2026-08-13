import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.*;
import java.nio.file.*;
import java.util.*;
import java.util.List;
import java.util.stream.*;
import java.util.prefs.Preferences;

/**
 * SoFD Randomizer USA PSP - ULUS10374 - ACCESSIBLE EDITION v2.0
 * Repo: bladesilburwolf-star/sofd_randomizer
 * 
 * Features:
 * - Minecraft-style UI with large buttons
 * - Font size selection 12-28+
 * - High contrast mode for low vision
 * - Scalable UI
 * - Accessibility options panel
 * 
 * @author James Greer + Meta AI + Claude - Accessible Edition
 */
public class PsmManTool extends JFrame {
    
    private JTextArea logArea;
    private JTextArea detailArea;
    private Path basePath;
    private Path outputPath;
    private long totalUnpacked = 0;
    
    // Accessibility
    private int currentFontSize = 16;
    private boolean highContrast = false;
    private boolean darkMode = true;
    private boolean minecraftStyle = true;
    private Preferences prefs = Preferences.userNodeForPackage(PsmManTool.class);
    private List<JComponent> allComponents = new ArrayList<>();
    private JComboBox<String> fontSizeCombo;
    private JCheckBox highContrastCheck;
    private JCheckBox darkModeCheck;
    private JCheckBox minecraftCheck;
    
    // Minecraft colors
    private static final Color MC_DIRT = new Color(134, 96, 67);
    private static final Color MC_GRASS = new Color(92, 142, 49);
    private static final Color MC_STONE = new Color(120, 120, 120);
    private static final Color MC_DARK = new Color(50, 50, 50);
    private static final Color MC_LIGHT = new Color(220, 220, 220);
    private static final Color MC_GREEN_DARK = new Color(60, 90, 30);
    private static final Color MC_GOLD = new Color(255, 215, 0);
    
    public PsmManTool() {
        loadPrefs();
        setTitle("SoFD Randomizer v2.0 ACCESSIBLE - USA PSP ULUS10374 - Minecraft Style - Font: " + currentFontSize);
        setSize(1400, 900);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        // Top panel with accessibility controls
        JPanel accessibilityPanel = createAccessibilityPanel();
        add(accessibilityPanel, BorderLayout.NORTH);
        
        // Main button panel - Minecraft style
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createTitledBorder("SoFD Randomizer USA PSP - Controls"));
        
        JPanel buttonGrid = new JPanel(new GridLayout(3, 4, 10, 10));
        buttonGrid.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        JButton btnSelect = createMCButton("1. Select ULUS10374 Folder", MC_GRASS);
        JButton btnOutput = createMCButton("2. Output Folder", MC_STONE);
        JButton btnUnpack = createMCButton("Unpack ALL (USA PSP)", new Color(80, 120, 200));
        JButton btnScan = createMCButton("Scan Tables", new Color(200, 160, 60));
        
        JButton btnChest = createMCButton("Random CHESTS", new Color(180, 120, 40));
        JButton btnShop = createMCButton("Random SHOPS", new Color(40, 180, 80));
        JButton btnEnemy = createMCButton("Random ENEMIES", new Color(180, 40, 40));
        JButton btnDrop = createMCButton("Random DROPS", new Color(160, 80, 160));
        
        JButton btnChar = createMCButton("Random CHARS", new Color(80, 160, 180));
        JButton btnRepack = createMCButton("Repack for PSP", new Color(60, 60, 160));
        JButton btnFull = createMCButton("FULL RANDO USA", MC_GOLD);
        btnFull.setForeground(Color.BLACK);
        btnFull.setFont(btnFull.getFont().deriveFont(Font.BOLD, currentFontSize + 2));
        
        JButton btnClear = createMCButton("Clear Logs", Color.DARK_GRAY);
        
        buttonGrid.add(btnSelect);
        buttonGrid.add(btnOutput);
        buttonGrid.add(btnUnpack);
        buttonGrid.add(btnScan);
        
        buttonGrid.add(btnChest);
        buttonGrid.add(btnShop);
        buttonGrid.add(btnEnemy);
        buttonGrid.add(btnDrop);
        
        buttonGrid.add(btnChar);
        buttonGrid.add(btnRepack);
        buttonGrid.add(btnFull);
        buttonGrid.add(btnClear);
        
        mainPanel.add(buttonGrid, BorderLayout.CENTER);
        
        // Split pane for logs - with big fonts
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setDividerLocation(700);
        
        logArea = new JTextArea();
        logArea.setEditable(false);
        detailArea = new JTextArea();
        detailArea.setEditable(false);
        
        JPanel left = new JPanel(new BorderLayout());
        JLabel leftLabel = new JLabel(" Log - USA PSP ULUS10374:");
        left.add(leftLabel, BorderLayout.NORTH);
        left.add(new JScrollPane(logArea), BorderLayout.CENTER);
        
        JPanel right = new JPanel(new BorderLayout());
        JLabel rightLabel = new JLabel(" SoFD USA Analysis - Chests/Shops/Enemies:");
        right.add(rightLabel, BorderLayout.NORTH);
        right.add(new JScrollPane(detailArea), BorderLayout.CENTER);
        
        allComponents.add(leftLabel);
        allComponents.add(rightLabel);
        
        split.setLeftComponent(left);
        split.setRightComponent(right);
        
        JPanel center = new JPanel(new BorderLayout());
        center.add(mainPanel, BorderLayout.NORTH);
        center.add(split, BorderLayout.CENTER);
        
        add(center, BorderLayout.CENTER);
        
        // Listeners
        btnSelect.addActionListener(e -> selectFolder());
        btnOutput.addActionListener(e -> selectOutput());
        btnUnpack.addActionListener(e -> new Thread(this::unpackAll).start());
        btnScan.addActionListener(e -> new Thread(this::scanSoFD).start());
        btnChest.addActionListener(e -> new Thread(this::randomizeChests).start());
        btnShop.addActionListener(e -> new Thread(this::randomizeShops).start());
        btnEnemy.addActionListener(e -> new Thread(this::randomizeEnemies).start());
        btnDrop.addActionListener(e -> new Thread(this::randomizeDrops).start());
        btnChar.addActionListener(e -> new Thread(this::randomizeChars).start());
        btnRepack.addActionListener(e -> new Thread(this::repackAll).start());
        btnFull.addActionListener(e -> new Thread(this::fullRandomize).start());
        btnClear.addActionListener(e -> { logArea.setText(""); detailArea.setText(""); log("Logs cleared"); });
        
        // Register buttons for font scaling
        for (Component c : buttonGrid.getComponents()) {
            if (c instanceof JButton) allComponents.add((JComponent)c);
        }
        
        applyAccessibilitySettings();
        
        Runtime rt = Runtime.getRuntime();
        log("=== SoFD Randomizer v2.0 ACCESSIBLE EDITION ===");
        log("Repo: bladesilburwolf-star/sofd_randomizer");
        log("USA PSP ULUS10374 - Minecraft Style - Font Size: " + currentFontSize + "pt");
        log("Accessibility: HighContrast=" + highContrast + " DarkMode=" + darkMode + " MinecraftStyle=" + minecraftStyle);
        log("JVM RAM: " + (rt.maxMemory()/(1024*1024)) + " MB - Use -Xmx4G for large ISO");
        log("PSP is tricky: BE and LE offset tables, nested PSM archives");
        log("Ready. Use font size selector above to adjust. Select ULUS10374 folder to start.");
    }
    
    private JPanel createAccessibilityPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        panel.setBorder(BorderFactory.createTitledBorder("Accessibility Options - Vision Friendly"));
        
        panel.add(new JLabel("Font Size:"));
        fontSizeCombo = new JComboBox<>(new String[]{"12", "14", "16", "18", "20", "22", "24", "26", "28", "32"});
        fontSizeCombo.setSelectedItem(String.valueOf(currentFontSize));
        fontSizeCombo.setToolTipText("Select font size - 14+ recommended for low vision");
        fontSizeCombo.addActionListener(e -> {
            currentFontSize = Integer.parseInt((String)fontSizeCombo.getSelectedItem());
            applyAccessibilitySettings();
            savePrefs();
        });
        panel.add(fontSizeCombo);
        
        highContrastCheck = new JCheckBox("High Contrast");
        highContrastCheck.setSelected(highContrast);
        highContrastCheck.setToolTipText("High contrast for better visibility");
        highContrastCheck.addActionListener(e -> {
            highContrast = highContrastCheck.isSelected();
            applyAccessibilitySettings();
            savePrefs();
        });
        panel.add(highContrastCheck);
        
        darkModeCheck = new JCheckBox("Dark Mode");
        darkModeCheck.setSelected(darkMode);
        darkModeCheck.setToolTipText("Dark background easier on eyes");
        darkModeCheck.addActionListener(e -> {
            darkMode = darkModeCheck.isSelected();
            applyAccessibilitySettings();
            savePrefs();
        });
        panel.add(darkModeCheck);
        
        minecraftCheck = new JCheckBox("Minecraft Style");
        minecraftCheck.setSelected(minecraftStyle);
        minecraftCheck.setToolTipText("Minecraft blocky style UI");
        minecraftCheck.addActionListener(e -> {
            minecraftStyle = minecraftCheck.isSelected();
            applyAccessibilitySettings();
            savePrefs();
        });
        panel.add(minecraftCheck);
        
        JButton resetBtn = new JButton("Reset to Default (16pt)");
        resetBtn.addActionListener(e -> {
            currentFontSize = 16;
            highContrast = false;
            darkMode = true;
            minecraftStyle = true;
            fontSizeCombo.setSelectedItem("16");
            highContrastCheck.setSelected(false);
            darkModeCheck.setSelected(true);
            minecraftCheck.setSelected(true);
            applyAccessibilitySettings();
            savePrefs();
        });
        panel.add(resetBtn);
        
        JLabel hint = new JLabel("Minecraft Style = Big Buttons + 14+ Fonts | Use mouse wheel to scroll logs");
        hint.setForeground(Color.GRAY);
        panel.add(hint);
        
        allComponents.add(panel);
        return panel;
    }
    
    private JButton createMCButton(String text, Color baseColor) {
        JButton btn = new JButton(text);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createRaisedBevelBorder());
        btn.setToolTipText(text);
        // Store base color for theming
        btn.putClientProperty("baseColor", baseColor);
        allComponents.add(btn);
        return btn;
    }
    
    private void applyAccessibilitySettings() {
        // Update title
        setTitle("SoFD Randomizer v2.0 ACCESSIBLE - USA PSP ULUS10374 - Font: " + currentFontSize + "pt" + (highContrast?" HIGH-CONTRAST":"") + (darkMode?" DARK":""));
        
        Font bigFont = new Font(Font.SANS_SERIF, Font.BOLD, currentFontSize);
        Font monoFont = new Font(Font.MONOSPACED, Font.PLAIN, Math.max(12, currentFontSize - 2));
        
        // Apply to text areas
        logArea.setFont(monoFont);
        detailArea.setFont(monoFont);
        
        // Apply to all registered components
        for (JComponent comp : allComponents) {
            if (comp instanceof JButton) {
                JButton btn = (JButton)comp;
                btn.setFont(bigFont);
                Color base = (Color)btn.getClientProperty("baseColor");
                if (base == null) base = MC_STONE;
                
                if (minecraftStyle) {
                    if (highContrast) {
                        btn.setBackground(darkMode ? Color.BLACK : Color.WHITE);
                        btn.setForeground(darkMode ? Color.YELLOW : Color.BLACK);
                        btn.setBorder(BorderFactory.createLineBorder(darkMode ? Color.YELLOW : Color.BLACK, 3));
                    } else if (darkMode) {
                        btn.setBackground(base.darker().darker());
                        btn.setForeground(Color.WHITE);
                        btn.setBorder(BorderFactory.createRaisedBevelBorder());
                    } else {
                        btn.setBackground(base);
                        btn.setForeground(Color.BLACK);
                        btn.setBorder(BorderFactory.createRaisedBevelBorder());
                    }
                } else {
                    // Standard style
                    btn.setBackground(null);
                    btn.setForeground(null);
                    btn.setBorder(UIManager.getBorder("Button.border"));
                }
            } else if (comp instanceof JLabel) {
                comp.setFont(new Font(Font.SANS_SERIF, Font.BOLD, currentFontSize));
                if (highContrast && darkMode) {
                    comp.setForeground(Color.YELLOW);
                } else if (darkMode) {
                    comp.setForeground(Color.WHITE);
                } else {
                    comp.setForeground(Color.BLACK);
                }
            }
        }
        
        // Apply to panels
        if (darkMode) {
            getContentPane().setBackground(new Color(30, 30, 30));
            logArea.setBackground(highContrast ? Color.BLACK : new Color(20, 20, 20));
            logArea.setForeground(highContrast ? Color.GREEN : Color.LIGHT_GRAY);
            detailArea.setBackground(highContrast ? Color.BLACK : new Color(20, 20, 20));
            detailArea.setForeground(highContrast ? Color.CYAN : Color.LIGHT_GRAY);
            logArea.setCaretColor(Color.WHITE);
            detailArea.setCaretColor(Color.WHITE);
        } else {
            getContentPane().setBackground(null);
            logArea.setBackground(highContrast ? Color.WHITE : new Color(250, 250, 250));
            logArea.setForeground(highContrast ? Color.BLACK : Color.BLACK);
            detailArea.setBackground(highContrast ? Color.WHITE : new Color(250, 250, 250));
            detailArea.setForeground(highContrast ? Color.BLACK : Color.BLACK);
            logArea.setCaretColor(Color.BLACK);
            detailArea.setCaretColor(Color.BLACK);
        }
        
        // Repaint
        revalidate();
        repaint();
    }
    
    private void loadPrefs() {
        try {
            currentFontSize = prefs.getInt("fontSize", 16);
            highContrast = prefs.getBoolean("highContrast", false);
            darkMode = prefs.getBoolean("darkMode", true);
            minecraftStyle = prefs.getBoolean("minecraftStyle", true);
            if (currentFontSize < 12) currentFontSize = 16;
        } catch(Exception e) {
            currentFontSize = 16;
        }
    }
    
    private void savePrefs() {
        try {
            prefs.putInt("fontSize", currentFontSize);
            prefs.putBoolean("highContrast", highContrast);
            prefs.putBoolean("darkMode", darkMode);
            prefs.putBoolean("minecraftStyle", minecraftStyle);
        } catch(Exception e) {}
    }
    
    void log(String s) { 
        SwingUtilities.invokeLater(() -> { 
            logArea.append(s + "\n"); 
            logArea.setCaretPosition(logArea.getDocument().getLength()); 
        }); 
    }
    
    void detail(String s) { 
        SwingUtilities.invokeLater(() -> { 
            detailArea.append(s + "\n"); 
        }); 
    }
    
    void selectFolder() {
        JFileChooser c = new JFileChooser("/home/james/PSP/SOFD");
        c.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        c.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, currentFontSize));
        if (c.showOpenDialog(this)==JFileChooser.APPROVE_OPTION) {
            basePath = c.getSelectedFile().toPath();
            if (Files.isRegularFile(basePath) && basePath.toString().toLowerCase().endsWith(".iso")) {
                log("ISO Selected: " + basePath);
            } else {
                log("Folder Selected: " + basePath);
            }
            try {
                if (Files.isDirectory(basePath)) {
                    Path usrdir = basePath.resolve("PSP_GAME").resolve("USRDIR");
                    if (Files.exists(usrdir)) { basePath = usrdir; log("Auto-switched to USRDIR: " + basePath); }
                }
            } catch(Exception e){}
        }
    }
    
    void selectOutput() {
        JFileChooser c = new JFileChooser(System.getProperty("user.home")+"/Documents/SOFD_Rando");
        c.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        c.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, currentFontSize));
        if (c.showOpenDialog(this)==JFileChooser.APPROVE_OPTION) {
            outputPath = c.getSelectedFile().toPath();
            log("Output: " + outputPath);
        }
    }
    
    boolean isArchive(Path file, ByteOrder[] detectedOrder) {
        try {
            if (!Files.isRegularFile(file)) return false;
            long sz = Files.size(file);
            if (sz < 16 || sz > 100_000_000) return false;
            if (file.getFileName().toString().equalsIgnoreCase("EBOOT.BIN")) return false;
            byte[] all = Files.readAllBytes(file);
            if (all.length < 16) return false;
            ByteBuffer le = ByteBuffer.wrap(all).order(ByteOrder.LITTLE_ENDIAN);
            int countLE = le.getInt(0);
            if (countLE > 0 && countLE < 20000) {
                if (all.length >= 4+countLE*4) {
                    int firstOff = le.getInt(4);
                    int tableSize = 4+countLE*4;
                    if (firstOff >= tableSize && firstOff < all.length && firstOff < 5_000_000) {
                        if (detectedOrder != null) detectedOrder[0] = ByteOrder.LITTLE_ENDIAN;
                        return true;
                    }
                }
            }
            ByteBuffer be = ByteBuffer.wrap(all).order(ByteOrder.BIG_ENDIAN);
            int countBE = be.getInt(0);
            if (countBE > 0 && countBE < 20000) {
                if (all.length >= 4+countBE*4) {
                    int firstOff = be.getInt(4);
                    int tableSize = 4+countBE*4;
                    if (firstOff >= tableSize && firstOff < all.length && firstOff < 5_000_000) {
                        if (detectedOrder != null) detectedOrder[0] = ByteOrder.BIG_ENDIAN;
                        return true;
                    }
                }
            }
            return false;
        } catch(Exception e){ return false; }
    }
    
    boolean unpackArchive(Path archiveFile, Path outDir, ByteOrder order) {
        try {
            byte[] all = Files.readAllBytes(archiveFile);
            ByteBuffer bb = ByteBuffer.wrap(all).order(order);
            int count = bb.getInt(0);
            if (count <=0 || count > 20000) return false;
            int[] offsets = new int[count];
            for (int i=0;i<count;i++) { try { offsets[i]=bb.getInt(4+i*4); } catch(Exception e){ offsets[i]=0; } }
            int[] sorted = Arrays.stream(offsets).filter(o->o>0 && o < all.length).distinct().sorted().toArray();
            if (sorted.length==0) return false;
            Files.createDirectories(outDir);
            int ok=0;
            for (int i=0;i<count;i++) {
                int start = offsets[i];
                if (start<=0 || start>=all.length) continue;
                int idx = Arrays.binarySearch(sorted, start);
                if (idx <0) idx = -idx-1;
                int end = (idx+1 < sorted.length) ? sorted[idx+1] : all.length;
                if (end<=start || end-start>20_000_000) continue;
                if (end-start < 1) continue;
                Files.write(outDir.resolve(String.format("%04d.bin", i)), Arrays.copyOfRange(all, start, end));
                ok++;
            }
            if (ok>0) {
                totalUnpacked++;
                log(String.format("Unpacked %s [%s] %d files -> %s", archiveFile.getFileName(), order==ByteOrder.BIG_ENDIAN?"BE":"LE", ok, outDir.getFileName()));
                return true;
            }
            return false;
        } catch(Exception e){ log("FAIL unpack "+archiveFile.getFileName()+" "+e.getMessage()); return false; }
    }
    
    void unpackAll() {
        if (basePath==null) { log("Select ULUS10374 folder first!"); return; }
        if (outputPath==null) outputPath = basePath.getParent().resolve("SOFD_USA_unpacked");
        try { Files.createDirectories(outputPath); } catch(Exception e){}
        log("=== Unpack ALL - USA PSP ULUS10374 ===");
        totalUnpacked=0;
        try (Stream<Path> walk = Files.walk(basePath, 3)) {
            List<Path> files = walk.filter(Files::isRegularFile).collect(Collectors.toList());
            for (Path p : files) {
                if (p.toString().toLowerCase().contains("unpacked")) continue;
                String name = p.getFileName().toString().toLowerCase();
                if (name.endsWith(".bin") || name.endsWith(".dat") || name.endsWith(".arc") || name.startsWith("data")) {
                    ByteOrder[] order = new ByteOrder[1];
                    if (isArchive(p, order)) {
                        String rel = basePath.relativize(p).toString().replace('/', '_').replace('\\', '_');
                        rel = rel.replaceAll("\\.(bin|dat|arc)$", "") + "_unpacked";
                        unpackArchive(p, outputPath.resolve(rel), order[0]);
                    }
                }
            }
        } catch(Exception e){ log("Walk err: "+e.getMessage()); }
        boolean foundMore=true; int loops=0;
        while (foundMore && loops < 8) {
            foundMore=false; loops++;
            try {
                List<Path> bins;
                try (Stream<Path> walk = Files.walk(outputPath)) {
                    bins = walk.filter(path->{
                        String s=path.toString().toLowerCase();
                        return s.endsWith(".bin") && !s.contains("_unpacked");
                    }).collect(Collectors.toList());
                }
                for (Path p : bins) {
                    ByteOrder[] order = new ByteOrder[1];
                    if (isArchive(p, order)) {
                        Path outDir = Paths.get(p.toString()+"_unpacked");
                        if (!Files.exists(outDir)) {
                            if (unpackArchive(p, outDir, order[0])) foundMore=true;
                        }
                    }
                }
            } catch(Exception e){ log("Recurse err: "+e.getMessage()); }
        }
        log("DONE. Total archives unpacked: " + totalUnpacked + " (USA PSP)");
    }
    
    void scanSoFD() {
        if (outputPath==null) { log("Unpack first!"); return; }
        log("=== SCAN: SoFD USA PSP Tables ===");
        detailArea.setText("");
        Map<Integer, Integer> hist = new TreeMap<>();
        int chestCandidates=0, shopCandidates=0, enemyCandidates=0, dropCandidates=0;
        StringBuilder sb = new StringBuilder();
        try (Stream<Path> walk = Files.walk(outputPath)) {
            List<Path> files = walk.filter(Files::isRegularFile).filter(p->{
                try { long sz=Files.size(p); return sz>=16 && sz<=8192; } catch(Exception e){return false;}
            }).collect(Collectors.toList());
            for (Path p : files) {
                try {
                    byte[] b = Files.readAllBytes(p);
                    long sz = b.length;
                    hist.put((int)sz, hist.getOrDefault((int)sz,0)+1);
                    boolean maybeChest = false, maybeShop=false;
                    if (sz >= 4 && sz <= 128 && sz % 2 ==0) {
                        ByteBuffer bb = ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN);
                        int lowVals=0;
                        for (int i=0;i<b.length/2;i++) {
                            int v = bb.getShort(i*2) & 0xFFFF;
                            if (v>0 && v < 4000) lowVals++;
                        }
                        if (lowVals >= b.length/2 * 0.6) { maybeChest=true; chestCandidates++; }
                    }
                    if (sz >= 64 && sz <= 2048 && sz % 4 ==0) { maybeShop=true; shopCandidates++; }
                    if (sz >= 200 && sz <= 1000) { enemyCandidates++; }
                    if (sz >= 32 && sz <= 512 && sz % 2 ==0) { dropCandidates++; }
                    if (maybeChest || (sz<=64 && hist.get((int)sz) < 30)) {
                        sb.append(String.format("%-55s %4d bytes %s\n", outputPath.relativize(p), sz, maybeChest?"[CHEST?]":""));
                    }
                } catch(Exception e){}
            }
            StringBuilder histSb = new StringBuilder("SoFD USA PSP Size Histogram:\n");
            histSb.append("Target: ULUS10374 Star Ocean First Departure USA\n\n");
            hist.entrySet().stream().sorted((a,b)->b.getValue()-a.getValue()).limit(40).forEach(e->histSb.append(String.format(" %4d bytes: %4d files\n", e.getKey(), e.getValue())));
            histSb.append(String.format("\nCandidates: CHEST~%d SHOP~%d ENEMY~%d DROP~%d\n", chestCandidates, shopCandidates, enemyCandidates, dropCandidates));
            histSb.append("\nSample files:\n").append(sb.toString());
            detailArea.setText(histSb.toString());
            log(String.format("Scan USA done: chest~%d shop~%d enemy~%d drop~%d", chestCandidates, shopCandidates, enemyCandidates, dropCandidates));
        } catch(Exception e){ log("Scan err: "+e.getMessage()); }
    }
    
    void randomizeChests() {
        if (outputPath==null) return;
        String seedStr = JOptionPane.showInputDialog(this, "Chest Seed (USA PSP):", "12345");
        if (seedStr==null) return;
        Random rnd = new Random(seedStr.hashCode());
        log("Randomizing CHESTS USA PSP seed "+seedStr);
        try {
            List<Path> chests = Files.walk(outputPath).filter(p->{
                try {
                    long sz=Files.size(p);
                    if (sz < 4 || sz > 256) return false;
                    if (!p.toString().endsWith(".bin")) return false;
                    String path = p.toString().toLowerCase();
                    if (!path.contains("field") && !path.contains("chest") && sz>64) return false;
                    byte[] b=Files.readAllBytes(p);
                    if (b.length %2 !=0) return false;
                    ByteBuffer bb = ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN);
                    int valid=0;
                    for (int i=0;i<b.length/2;i++) { int v=bb.getShort(i*2) & 0xFFFF; if (v>0 && v<4000) valid++; }
                    return valid >= b.length/2 * 0.5;
                } catch(Exception e){return false;}
            }).collect(Collectors.toList());
            for (Path p : chests) {
                try {
                    byte[] b=Files.readAllBytes(p);
                    ByteBuffer bb = ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN);
                    List<Integer> items = new ArrayList<>();
                    for (int i=0;i<b.length/2;i++) items.add((int)(bb.getShort(i*2) & 0xFFFF));
                    Collections.shuffle(items, rnd);
                    ByteBuffer out = ByteBuffer.allocate(b.length).order(ByteOrder.LITTLE_ENDIAN);
                    for (int v : items) out.putShort((short)v);
                    Files.write(p, out.array());
                } catch(Exception e){}
            }
            log("Shuffled "+chests.size()+" chest tables (USA PSP)");
        } catch(Exception e){ log("Chest rando err: "+e.getMessage()); }
    }
    
    void randomizeShops() {
        if (outputPath==null) return;
        String seedStr = JOptionPane.showInputDialog(this, "Shop Seed (USA):", "54321");
        if (seedStr==null) return;
        Random rnd = new Random(seedStr.hashCode());
        log("Randomizing SHOPS USA seed "+seedStr);
        try {
            List<Path> shops = Files.walk(outputPath).filter(p->{
                try { long sz=Files.size(p); String s=p.toString().toLowerCase(); return sz>=64 && sz<=4096 && (s.contains("shop") || sz==256 || sz==512); } catch(Exception e){return false;}
            }).collect(Collectors.toList());
            Map<Long, List<Path>> bySize = new HashMap<>();
            for (Path p : shops) { try { long sz=Files.size(p); bySize.computeIfAbsent(sz, k->new ArrayList<>()).add(p); } catch(Exception e){} }
            int total=0;
            for (Map.Entry<Long, List<Path>> e : bySize.entrySet()) {
                List<Path> group = e.getValue(); if (group.size() < 2) continue;
                List<byte[]> datas = new ArrayList<>();
                for (Path p : group) try { datas.add(Files.readAllBytes(p)); } catch(Exception ex){}
                Collections.shuffle(datas, rnd);
                for (int i=0;i<group.size() && i<datas.size();i++) { try { Files.write(group.get(i), datas.get(i)); total++; } catch(Exception ex){} }
            }
            log("Shuffled "+total+" shop tables (USA)");
        } catch(Exception ex){ log("Shop err: "+ex.getMessage()); }
    }
    
    void randomizeEnemies() {
        if (outputPath==null) return;
        String seedStr = JOptionPane.showInputDialog(this, "Enemy Seed (USA):", "99999");
        if (seedStr==null) return;
        Random rnd = new Random(seedStr.hashCode());
        log("Randomizing ENEMIES USA PSP seed "+seedStr);
        try {
            List<Path> enemies = Files.walk(outputPath).filter(p->{
                try { long sz=Files.size(p); String s=p.toString().toLowerCase(); return (sz>=200 && sz<=1200) && (s.contains("battle") || s.contains("enemy") || sz==470 || sz==512); } catch(Exception e){return false;}
            }).collect(Collectors.toList());
            Map<Long, List<Path>> bySize = new HashMap<>();
            for (Path p : enemies) { try { long sz=Files.size(p); bySize.computeIfAbsent(sz, k->new ArrayList<>()).add(p); } catch(Exception e){} }
            for (Map.Entry<Long, List<Path>> entry : bySize.entrySet()) {
                List<Path> group = entry.getValue(); if (group.size() < 2) continue;
                List<byte[]> datas = new ArrayList<>();
                for (Path p : group) try { datas.add(Files.readAllBytes(p)); } catch(Exception e){}
                Collections.shuffle(datas, rnd);
                for (int i=0;i<group.size() && i<datas.size();i++) { try { Files.write(group.get(i), datas.get(i)); } catch(Exception e){} }
                log("Shuffled "+group.size()+" enemies size "+entry.getKey()+" (USA)");
            }
        } catch(Exception e){ log("Enemy err: "+e.getMessage()); }
    }
    
    void randomizeDrops() {
        if (outputPath==null) return;
        String seedStr = JOptionPane.showInputDialog(this, "Drop Seed (USA):", "77777");
        if (seedStr==null) return;
        Random rnd = new Random(seedStr.hashCode());
        log("Randomizing DROPS USA seed "+seedStr);
        try {
            List<Path> drops = Files.walk(outputPath).filter(p->{
                try { long sz=Files.size(p); String s=p.toString().toLowerCase(); return sz>=16 && sz<=512 && (s.contains("drop") || s.contains("item") || sz==32 || sz==64); } catch(Exception e){return false;}
            }).collect(Collectors.toList());
            for (Path p : drops) {
                try {
                    byte[] b=Files.readAllBytes(p);
                    if (b.length %2==0) {
                        ByteBuffer bb = ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN);
                        List<Short> ids = new ArrayList<>();
                        for (int i=0;i<b.length/2;i++) ids.add(bb.getShort(i*2));
                        Collections.shuffle(ids, rnd);
                        ByteBuffer out = ByteBuffer.allocate(b.length).order(ByteOrder.LITTLE_ENDIAN);
                        for (short s : ids) out.putShort(s);
                        Files.write(p, out.array());
                    }
                } catch(Exception e){}
            }
            log("Shuffled "+drops.size()+" drop tables (USA)");
        } catch(Exception e){ log("Drop err: "+e.getMessage()); }
    }
    
    void randomizeChars() {
        if (outputPath==null) return;
        log("Randomizing CHARS - starting stats, talents");
        try {
            List<Path> charFiles = Files.walk(outputPath).filter(p->{
                try { long sz=Files.size(p); return sz>=64 && sz<=1024 && p.toString().toLowerCase().contains("char"); } catch(Exception e){return false;}
            }).collect(Collectors.toList());
            log("Found "+charFiles.size()+" char files");
            detailArea.setText("CHAR FILES:\n");
            for (Path p : charFiles) detailArea.append(p.toString()+"\n");
        } catch(Exception e){ log("Char err: "+e.getMessage()); }
    }
    
    void repackAll() {
        if (outputPath==null) return;
        log("=== Repack ALL - USA PSP ULUS10374 ===");
        try {
            Files.walk(outputPath).filter(p->Files.isDirectory(p) && p.getFileName().toString().endsWith("_unpacked")).forEach(src->{
                try {
                    List<Path> files = Files.list(src).filter(f->f.toString().endsWith(".bin")).sorted().collect(Collectors.toList());
                    files.sort((a,b)->{ try { int na=Integer.parseInt(a.getFileName().toString().replaceAll("[^0-9]","")); int nb=Integer.parseInt(b.getFileName().toString().replaceAll("[^0-9]","")); return Integer.compare(na, nb); } catch(Exception e){return a.compareTo(b);} });
                    if (files.isEmpty()) return;
                    int count=files.size();
                    ByteBuffer hdr = ByteBuffer.allocate(4+count*4).order(ByteOrder.LITTLE_ENDIAN);
                    hdr.putInt(count); int cur=4+count*4;
                    List<byte[]> datas=new ArrayList<>();
                    for (Path f: files) { byte[] d=Files.readAllBytes(f); datas.add(d); hdr.putInt(cur); cur+=d.length; }
                    ByteArrayOutputStream baos=new ByteArrayOutputStream(); baos.write(hdr.array());
                    for (byte[] d: datas) baos.write(d);
                    Path dest = src.getParent().resolve(src.getFileName().toString().replace("_unpacked","") + ".repacked");
                    Files.write(dest, baos.toByteArray());
                    log("REPACKED "+dest.getFileName()+" ("+count+" files) [USA LE]");
                    ByteBuffer hdrBE = ByteBuffer.allocate(4+count*4).order(ByteOrder.BIG_ENDIAN);
                    hdrBE.putInt(count); cur=4+count*4;
                    for (byte[] d: datas) { hdrBE.putInt(cur); cur+=d.length; }
                    ByteArrayOutputStream baosBE=new ByteArrayOutputStream(); baosBE.write(hdrBE.array());
                    for (byte[] d: datas) baosBE.write(d);
                    Path destBE = src.getParent().resolve(src.getFileName().toString().replace("_unpacked","") + ".repacked.BE");
                    Files.write(destBE, baosBE.toByteArray());
                } catch(Exception e){ log("Fail repack "+src.getFileName()+" "+e.getMessage()); }
            });
            log("Repack done. Replace original BINs with .repacked");
        } catch(Exception e){ log("Repack err: "+e.getMessage()); }
    }
    
    void fullRandomize() {
        String seed = JOptionPane.showInputDialog(this, "FULL RANDO SEED USA PSP:", "123456");
        if (seed==null) return;
        log("=== FULL RANDOMIZER USA PSP SEED: "+seed+" ===");
        log("Chest: "+seed+"_chest USA | Shop: "+seed+"_shop | Enemy: "+seed+"_enemy | Drop: "+seed+"_drop");
        log("Run each randomizer button with these seeds for deterministic run");
    }
    
    public static void main(String[] args){
        // Set system look and feel with bigger fonts
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            // Increase default font for all UI
            Font defaultFont = new Font(Font.SANS_SERIF, Font.PLAIN, 16);
            UIManager.put("Button.font", defaultFont);
            UIManager.put("Label.font", defaultFont);
            UIManager.put("TextArea.font", new Font(Font.MONOSPACED, Font.PLAIN, 14));
            UIManager.put("ComboBox.font", defaultFont);
            UIManager.put("CheckBox.font", defaultFont);
        } catch(Exception e) {}
        SwingUtilities.invokeLater(()->new PsmManTool().setVisible(true));
    }
}
