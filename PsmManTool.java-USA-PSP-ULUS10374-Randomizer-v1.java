import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.nio.*;
import java.nio.file.*;
import java.util.*;
import java.util.List;
import java.util.stream.*;

/**
 * Star Ocean: First Departure (PSP) Randomizer
 * Repo: bladesilburwolf-star/sofd_randomizer
 * 
 * Takes over from Mistral/Codex - PSP is tricky:
 * - Big-endian vs little-endian detection
 * - ISO UMD structure (PSP_GAME/USRDIR)
 * - Offset table archives similar to PSM but BE
 * - Chest, shop, enemy, character tables
 * 
 * Works on extracted ISO folder or direct PSP_GAME folder.
 * 
 * @author James Greer + Meta AI + Claude
 */
public class PsmManTool extends JFrame {
    
    private JTextArea logArea;
    private JTextArea detailArea;
    private Path basePath;
    private Path outputPath;
    private long totalUnpacked = 0;
    
    // SOFD specific magic
    private static final int[] ITEM_SIZES = {32, 48, 64, 128}; // common chest/shop sizes
    private Map<Long, Integer> sizeHistogram = new HashMap<>();
    
    public PsmManTool() {
        setTitle("SoFD Randomizer v1.0 - Star Ocean First Departure (PSP) - PSP Tricky Edition");
        setSize(1250, 750);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        JPanel top = new JPanel(new GridLayout(3,1));
        JPanel r1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JPanel r2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JPanel r3 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        JButton btnSelect = new JButton("1. Select SOFD Folder/ISO");
        JButton btnOutput = new JButton("2. Output Folder");
        JButton btnUnpack = new JButton("Unpack ALL (PSM+BE)");
        JButton btnScan = new JButton("Scan Tables");
        JButton btnChest = new JButton("Random CHESTS");
        JButton btnShop = new JButton("Random SHOPS");
        JButton btnEnemy = new JButton("Random ENEMIES");
        JButton btnChar = new JButton("Random CHARS");
        JButton btnRepack = new JButton("Repack ISO");
        JButton btnFullRando = new JButton("FULL RANDOMIZER");
        
        r1.add(btnSelect); r1.add(btnOutput); r1.add(btnUnpack);
        r2.add(btnScan); r2.add(btnChest); r2.add(btnShop); r2.add(btnEnemy);
        r3.add(btnChar); r3.add(btnRepack); r3.add(btnFullRando);
        
        top.add(r1); top.add(r2); top.add(r3);
        add(top, BorderLayout.NORTH);
        
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setDividerLocation(700);
        logArea = new JTextArea(); logArea.setFont(new Font("Consolas", Font.PLAIN, 11));
        detailArea = new JTextArea(); detailArea.setFont(new Font("Consolas", Font.PLAIN, 11));
        JPanel left = new JPanel(new BorderLayout());
        left.add(new JLabel(" Log - PSP Tricky:"), BorderLayout.NORTH);
        left.add(new JScrollPane(logArea), BorderLayout.CENTER);
        JPanel right = new JPanel(new BorderLayout());
        right.add(new JLabel(" Analysis / Chest/Shop Data:"), BorderLayout.NORTH);
        right.add(new JScrollPane(detailArea), BorderLayout.CENTER);
        split.setLeftComponent(left); split.setRightComponent(right);
        add(split, BorderLayout.CENTER);
        
        btnSelect.addActionListener(e -> selectFolder());
        btnOutput.addActionListener(e -> selectOutput());
        btnUnpack.addActionListener(e -> new Thread(this::unpackAll).start());
        btnScan.addActionListener(e -> new Thread(this::scanTables).start());
        btnChest.addActionListener(e -> new Thread(this::randomizeChests).start());
        btnShop.addActionListener(e -> new Thread(this::randomizeShops).start());
        btnEnemy.addActionListener(e -> new Thread(this::randomizeEnemies).start());
        btnChar.addActionListener(e -> new Thread(this::randomizeChars).start());
        btnRepack.addActionListener(e -> new Thread(this::repackAll).start());
        btnFullRando.addActionListener(e -> new Thread(this::fullRandomize).start());
        
        Runtime rt = Runtime.getRuntime();
        log("SoFD Randomizer v1.0 - For PSP Star Ocean First Departure");
        log("Repo: bladesilburwolf-star/sofd_randomizer");
        log("JVM RAM: " + (rt.maxMemory()/(1024*1024)) + " MB - Use -Xmx4G for large ISO");
        log("PSP is tricky: supports BE and LE offset tables, nested PSM archives");
        log("Ready. Select extracted ISO folder or PSP_GAME folder");
    }
    
    void log(String s) { SwingUtilities.invokeLater(() -> { logArea.append(s + "\n"); logArea.setCaretPosition(logArea.getDocument().getLength()); }); }
    void detail(String s) { SwingUtilities.invokeLater(() -> { detailArea.append(s + "\n"); }); }
    
    void selectFolder() {
        JFileChooser c = new JFileChooser("/home/james/PSP/SOFD");
        c.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        if (c.showOpenDialog(this)==JFileChooser.APPROVE_OPTION) {
            basePath = c.getSelectedFile().toPath();
            if (Files.isRegularFile(basePath) && basePath.toString().toLowerCase().endsWith(".iso")) {
                log("ISO Selected: " + basePath + " - Will need to extract or use UMDGen logic");
            } else {
                log("Folder Selected: " + basePath);
            }
            // Auto-detect USRDIR
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
        if (c.showOpenDialog(this)==JFileChooser.APPROVE_OPTION) {
            outputPath = c.getSelectedFile().toPath();
            log("Output: " + outputPath);
        }
    }
    
    // PSP Archive detection: supports both LE (PSX style) and BE (PSP native)
    boolean isArchive(Path file, ByteOrder[] detectedOrder) {
        try {
            if (!Files.isRegularFile(file)) return false;
            long sz = Files.size(file);
            if (sz < 16 || sz > 100_000_000) return false;
            byte[] all = Files.readAllBytes(file);
            
            // Try LE
            ByteBuffer le = ByteBuffer.wrap(all).order(ByteOrder.LITTLE_ENDIAN);
            int countLE = le.getInt(0);
            if (countLE > 0 && countLE < 20000) {
                int firstOff = le.getInt(4);
                if (firstOff >= 4+countLE*4 && firstOff < all.length && firstOff < 1000000) {
                    if (detectedOrder != null) detectedOrder[0] = ByteOrder.LITTLE_ENDIAN;
                    return true;
                }
            }
            // Try BE
            ByteBuffer be = ByteBuffer.wrap(all).order(ByteOrder.BIG_ENDIAN);
            int countBE = be.getInt(0);
            if (countBE > 0 && countBE < 20000) {
                int firstOff = be.getInt(4);
                if (firstOff >= 4+countBE*4 && firstOff < all.length && firstOff < 1000000) {
                    if (detectedOrder != null) detectedOrder[0] = ByteOrder.BIG_ENDIAN;
                    return true;
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
            for (int i=0;i<count;i++) {
                try { offsets[i]=bb.getInt(4+i*4); } catch(Exception e){ offsets[i]=0; }
            }
            
            // Sort for end detection (optimized from Darkstone E6700 fix)
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
        if (basePath==null) { log("Select folder first!"); return; }
        if (outputPath==null) outputPath = basePath.getParent().resolve(basePath.getFileName()+"_unpacked");
        try { Files.createDirectories(outputPath); } catch(Exception e){}
        log("=== Unpack ALL - PSP Tricky Mode (LE+BE) ===");
        totalUnpacked=0;
        
        // PASS 1: Find all archives in basePath
        try (Stream<Path> walk = Files.walk(basePath, 2)) {
            List<Path> files = walk.filter(Files::isRegularFile).collect(Collectors.toList());
            for (Path p : files) {
                if (p.toString().toLowerCase().contains("unpacked")) continue;
                String name = p.getFileName().toString().toLowerCase();
                if (name.endsWith(".bin") || name.endsWith(".dat") || name.endsWith(".psm") || name.endsWith(".dps") || name.endsWith(".arc")) {
                    ByteOrder[] order = new ByteOrder[1];
                    if (isArchive(p, order)) {
                        String rel = basePath.relativize(p).toString().replace('/', '_').replace('\\', '_');
                        rel = rel.replaceAll("\\.(bin|dat|psm|dps|arc)$", "") + "_unpacked";
                        unpackArchive(p, outputPath.resolve(rel), order[0]);
                    }
                }
            }
        } catch(Exception e){ log("Walk err: "+e.getMessage()); }
        
        // PASS 2: Recursive nested (PSP loves nested archives)
        boolean foundMore=true; int loops=0;
        while (foundMore && loops < 6) {
            foundMore=false; loops++;
            try {
                List<Path> bins;
                try (Stream<Path> walk = Files.walk(outputPath)) {
                    bins = walk.filter(path->path.toString().toLowerCase().endsWith(".bin")).collect(Collectors.toList());
                }
                for (Path p : bins) {
                    if (p.toString().contains("_unpacked")) continue;
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
        log("DONE. Total archives unpacked: " + totalUnpacked);
    }
    
    void scanTables() {
        if (outputPath==null) { log("Unpack first!"); return; }
        log("=== SCAN: SoFD Tables - Chests, Shops, Enemies, Char Stats ===");
        detailArea.setText("");
        Map<Integer, Integer> hist = new TreeMap<>();
        int chestCandidates=0, shopCandidates=0, enemyCandidates=0;
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
                    
                    // Heuristic: chest table? often array of u16 item IDs
                    // Shop table? larger, contains price data
                    // Enemy? size 400-1500 like Darkstone but BE
                    String txt = new String(b, "ISO-8859-1").toLowerCase();
                    
                    boolean maybeChest = false, maybeShop=false, maybeEnemy=false;
                    
                    if (sz >= 32 && sz <= 256 && sz % 2 ==0) {
                        // Check if looks like item ID list (low values < 2000)
                        ByteBuffer bb = ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN);
                        int lowVals=0;
                        for (int i=0;i<b.length/2;i++) {
                            int v = bb.getShort(i*2) & 0xFFFF;
                            if (v < 5000) lowVals++;
                        }
                        if (lowVals > b.length/2 * 0.7) { maybeChest=true; chestCandidates++; }
                    }
                    if (sz >= 128 && sz <= 2048 && sz % 4 ==0) {
                        // Shop: item ID + price pattern
                        maybeShop=true; shopCandidates++;
                    }
                    if ((sz==470 || sz==934 || sz==1398 || sz==1870) || (sz>=400 && sz<=2000)) {
                        String lower = new String(b, "ISO-8859-1");
                        if (lower.contains("ITEM") || b[0]==0x01 || sz % 2 ==0) { maybeEnemy=true; enemyCandidates++; }
                    }
                    
                    if (maybeChest || maybeShop || (sz<=64 && hist.get((int)sz) < 20)) {
                        StringBuilder hex = new StringBuilder();
                        for (int i=0;i<Math.min(32,b.length);i++) hex.append(String.format("%02X ", b[i]));
                        sb.append(String.format("%-50s %4d bytes %s %s\n", outputPath.relativize(p), sz, 
                            maybeChest?"[CHEST?]":"", maybeShop?"[SHOP?]":""));
                        sb.append("  HEX: "+hex.toString().substring(0, Math.min(96, hex.length()))+"\n");
                    }
                } catch(Exception e){}
            }
            StringBuilder histSb = new StringBuilder("Size Histogram (common sizes = tables):\n");
            hist.entrySet().stream().sorted((a,b)->b.getValue()-a.getValue()).limit(30).forEach(e->histSb.append(String.format(" %4d bytes: %4d files\n", e.getKey(), e.getValue())));
            histSb.append(String.format("\nCandidates: CHEST=%d SHOP=%d ENEMY=%d\n", chestCandidates, shopCandidates, enemyCandidates));
            histSb.append("\nSample files:\n").append(sb.toString());
            detailArea.setText(histSb.toString());
            log(String.format("Scan done: %d files, chest~%d shop~%d enemy~%d", hist.values().stream().mapToInt(Integer::intValue).sum(), chestCandidates, shopCandidates, enemyCandidates));
        } catch(Exception e){ log("Scan err: "+e.getMessage()); }
    }
    
    void randomizeChests() {
        if (outputPath==null) return;
        String seedStr = JOptionPane.showInputDialog(this, "Chest Seed:", "12345");
        if (seedStr==null) return;
        Random rnd = new Random(seedStr.hashCode());
        log("Randomizing CHESTS seed "+seedStr+" hash "+seedStr.hashCode());
        try {
            // Find chest-like files: 32-256 bytes, item ID list
            List<Path> chests = Files.walk(outputPath).filter(p->{
                try {
                    long sz=Files.size(p);
                    if (sz < 16 || sz > 512) return false;
                    if (!p.toString().endsWith(".bin")) return false;
                    byte[] b=Files.readAllBytes(p);
                    // Check low distinct item IDs
                    if (b.length %2 !=0) return false;
                    ByteBuffer bb = ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN);
                    int valid=0;
                    for (int i=0;i<b.length/2;i++) {
                        int v=bb.getShort(i*2) & 0xFFFF;
                        if (v>0 && v<5000) valid++;
                    }
                    return valid > 2;
                } catch(Exception e){return false;}
            }).collect(Collectors.toList());
            
            for (Path p : chests) {
                try {
                    byte[] b=Files.readAllBytes(p);
                    ByteBuffer bb = ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN);
                    List<Short> items = new ArrayList<>();
                    for (int i=0;i<b.length/2;i++) items.add(bb.getShort(i*2));
                    Collections.shuffle(items, rnd);
                    ByteBuffer out = ByteBuffer.allocate(b.length).order(ByteOrder.LITTLE_ENDIAN);
                    for (short s : items) out.putShort(s);
                    Files.write(p, out.array());
                } catch(Exception e){}
            }
            log("Shuffled "+chests.size()+" chest tables");
        } catch(Exception e){ log("Chest rando err: "+e.getMessage()); }
    }
    
    void randomizeShops() {
        if (outputPath==null) return;
        String seedStr = JOptionPane.showInputDialog(this, "Shop Seed:", "54321");
        if (seedStr==null) return;
        Random rnd = new Random(seedStr.hashCode());
        log("Randomizing SHOPS seed "+seedStr);
        try {
            List<Path> shops = Files.walk(outputPath).filter(p->{
                try {
                    long sz=Files.size(p);
                    return sz>=128 && sz<=4096 && p.toString().endsWith(".bin");
                } catch(Exception e){return false;}
            }).collect(Collectors.toList());
            
            // Group by size (same shop type)
            Map<Long, List<Path>> bySize = new HashMap<>();
            for (Path p : shops) {
                try { long sz=Files.size(p); bySize.computeIfAbsent(sz, k->new ArrayList<>()).add(p); } catch(Exception e){}
            }
            int total=0;
            for (Map.Entry<Long, List<Path>> e : bySize.entrySet()) {
                List<Path> group = e.getValue();
                if (group.size() < 2) continue;
                List<byte[]> datas = new ArrayList<>();
                for (Path p : group) try { datas.add(Files.readAllBytes(p)); } catch(Exception ex){}
                Collections.shuffle(datas, rnd);
                for (int i=0;i<group.size() && i<datas.size();i++) {
                    try { Files.write(group.get(i), datas.get(i)); total++; } catch(Exception ex){}
                }
            }
            log("Shuffled "+total+" shop tables by size group");
        } catch(Exception ex){ log("Shop err: "+ex.getMessage()); }
    }
    
    void randomizeEnemies() {
        if (outputPath==null) return;
        String seedStr = JOptionPane.showInputDialog(this, "Enemy Seed:", "99999");
        if (seedStr==null) return;
        Random rnd = new Random(seedStr.hashCode());
        log("Randomizing ENEMIES seed "+seedStr);
        try {
            List<Path> enemies = Files.walk(outputPath).filter(p->{
                try {
                    long sz=Files.size(p);
                    return (sz>=400 && sz<=3000) && p.toString().endsWith(".bin");
                } catch(Exception e){return false;}
            }).collect(Collectors.toList());
            
            Map<Long, List<Path>> bySize = new HashMap<>();
            for (Path p : enemies) {
                try { long sz=Files.size(p); bySize.computeIfAbsent(sz, k->new ArrayList<>()).add(p); } catch(Exception e){}
            }
            for (Map.Entry<Long, List<Path>> entry : bySize.entrySet()) {
                List<Path> group = entry.getValue();
                if (group.size() < 2) continue;
                List<byte[]> datas = new ArrayList<>();
                for (Path p : group) try { datas.add(Files.readAllBytes(p)); } catch(Exception e){}
                Collections.shuffle(datas, rnd);
                for (int i=0;i<group.size() && i<datas.size();i++) {
                    try { Files.write(group.get(i), datas.get(i)); } catch(Exception e){}
                }
                log("Shuffled "+group.size()+" enemies size "+entry.getKey());
            }
        } catch(Exception e){ log("Enemy err: "+e.getMessage()); }
    }
    
    void randomizeChars() {
        if (outputPath==null) return;
        log("Randomizing CHARS - starting stats, talents");
        try {
            // Char stat tables are often 128-512 bytes with talent data
            List<Path> charFiles = Files.walk(outputPath).filter(p->{
                try {
                    long sz=Files.size(p);
                    return sz>=64 && sz<=1024 && p.toString().toLowerCase().contains("char");
                } catch(Exception e){return false;}
            }).collect(Collectors.toList());
            log("Found "+charFiles.size()+" char files by name, plus scanning by size");
            // Also scan all small files for stat-like patterns
            List<Path> statFiles = Files.walk(outputPath).filter(p->{
                try {
                    long sz=Files.size(p);
                    return sz>=32 && sz<=256;
                } catch(Exception e){return false;}
            }).limit(100).collect(Collectors.toList());
            log("Char randomization: review detail panel for candidates - manual edit recommended for now");
            detailArea.setText("CHAR FILES (review before randomizing):\n");
            for (Path p : charFiles) detailArea.append(p.toString()+"\n");
        } catch(Exception e){ log("Char err: "+e.getMessage()); }
    }
    
    void repackAll() {
        if (outputPath==null) return;
        log("=== Repack ALL - PSP BE/LE aware ===");
        try {
            Files.walk(outputPath).filter(p->Files.isDirectory(p) && p.getFileName().toString().endsWith("_unpacked")).forEach(src->{
                try {
                    List<Path> files = Files.list(src).filter(f->f.toString().endsWith(".bin")).sorted().collect(Collectors.toList());
                    files.sort((a,b)->{
                        try {
                            int na=Integer.parseInt(a.getFileName().toString().replaceAll("[^0-9]",""));
                            int nb=Integer.parseInt(b.getFileName().toString().replaceAll("[^0-9]",""));
                            return Integer.compare(na, nb);
                        } catch(Exception e){return a.compareTo(b);}
                    });
                    if (files.isEmpty()) return;
                    // Detect original order from parent archive if possible - try BE first for PSP
                    int count=files.size();
                    // Try to guess original byte order by checking parent file
                    ByteOrder order = ByteOrder.LITTLE_ENDIAN;
                    Path parentArchive = src.getParent().resolve(src.getFileName().toString().replace("_unpacked",""));
                    // Default to LE for SoFD? Actually PSP is BE but SoFD may be LE for compatibility
                    // We'll write both LE and check size
                    ByteBuffer hdr = ByteBuffer.allocate(4+count*4).order(ByteOrder.LITTLE_ENDIAN);
                    hdr.putInt(count);
                    int cur=4+count*4;
                    List<byte[]> datas=new ArrayList<>();
                    for (Path f: files) { byte[] d=Files.readAllBytes(f); datas.add(d); hdr.putInt(cur); cur+=d.length; }
                    ByteArrayOutputStream baos=new ByteArrayOutputStream();
                    baos.write(hdr.array());
                    for (byte[] d: datas) baos.write(d);
                    Path dest = src.getParent().resolve(src.getFileName().toString().replace("_unpacked","") + ".repacked");
                    Files.write(dest, baos.toByteArray());
                    log("REPACKED "+dest.getFileName()+" ("+count+" files)");
                    
                    // Also write BE version for PSP
                    ByteBuffer hdrBE = ByteBuffer.allocate(4+count*4).order(ByteOrder.BIG_ENDIAN);
                    hdrBE.putInt(count);
                    cur=4+count*4;
                    for (byte[] d: datas) { hdrBE.putInt(cur); cur+=d.length; }
                    ByteArrayOutputStream baosBE=new ByteArrayOutputStream();
                    baosBE.write(hdrBE.array());
                    for (byte[] d: datas) baosBE.write(d);
                    Path destBE = src.getParent().resolve(src.getFileName().toString().replace("_unpacked","") + ".repacked.BE");
                    Files.write(destBE, baosBE.toByteArray());
                    
                } catch(Exception e){ log("Fail repack "+src.getFileName()+" "+e.getMessage()); }
            });
        } catch(Exception e){ log("Repack err: "+e.getMessage()); }
    }
    
    void fullRandomize() {
        String seed = JOptionPane.showInputDialog(this, "FULL RANDOMIZER SEED:", "123456");
        if (seed==null) return;
        log("=== FULL RANDOMIZER SEED: "+seed+" ===");
        // Chain all randomizations with same seed base
        // Use seed hash + offsets for each category to keep deterministic
        new Thread(() -> {
            try {
                // Chest
                Random rnd = new Random(seed.hashCode());
                log("Step 1/4: Chests...");
                // Call internal without dialog
                // We will reuse logic but with derived seeds
                String chestSeed = seed + "_chest";
                String shopSeed = seed + "_shop";
                String enemySeed = seed + "_enemy";
                String charSeed = seed + "_char";
                // For demo, just log - actual would call methods with seed
                log("Chest seed: "+chestSeed+" -> hash "+chestSeed.hashCode());
                log("Shop seed: "+shopSeed);
                log("Enemy seed: "+enemySeed);
                log("Char seed: "+charSeed);
                log("FULL RANDO: Run each button with these seeds, or implement auto-rando in next version");
                log("TODO: Auto-chain randomizations with seed offsets");
            } catch(Exception e){ log("Full rando err: "+e.getMessage()); }
        }).start();
    }
    
    public static void main(String[] args){
        SwingUtilities.invokeLater(()->new PsmManTool().setVisible(true));
    }
}
