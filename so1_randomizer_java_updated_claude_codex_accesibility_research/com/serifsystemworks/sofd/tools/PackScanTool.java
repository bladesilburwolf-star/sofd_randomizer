package com.serifsystemworks.sofd.tools;

import java.io.*;
import java.nio.*;
import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.util.*;

public class PackScanTool {

    static final int SECTOR = 2048;
    static final int MIN_RUN = 16;
    static final int MIN_WORDS = 8;
    static final int MAX_CONTAINER = 8 * 1024 * 1024;
    static final int TOP_N = 150;

    static class Hit implements Comparable<Hit> {
        long fileOffset;
        int runLength;
        int wordCount;
        String sample;
        boolean fromSlz;
        int slzMode = -1;
        int slzOutSize = -1;

        public int compareTo(Hit o) { return Integer.compare(o.runLength, runLength); }
    }

    static final Comparator<Hit> BY_WORD_COUNT = (a, b) -> Integer.compare(b.wordCount, a.wordCount);

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.out.println("Usage: java com.serifsystemworks.sofd.tools.PackScanTool <path to so1pack.bin>");
            return;
        }
        File f = new File(args[0]);
        if (!f.isFile()) {
            System.out.println("File not found: " + f.getAbsolutePath());
            return;
        }
        long fileLen = f.length();
        System.out.println("Scanning " + f.getName() + "  (" + fileLen + " bytes, " + (fileLen / (1024 * 1024)) + " MB)");

        // Create output directory for decompressed SLZ containers
        File outputDir = new File("slz_decompressed");
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }
        System.out.println("Decompressed SLZ containers will be saved to: " + outputDir.getAbsolutePath());

        List<Hit> rawHits = new ArrayList<>();
        List<Hit> slzHits = new ArrayList<>();
        long slzContainers = 0, slzFailed = 0, slzSaved = 0;

        try (RandomAccessFile raf = new RandomAccessFile(f, "r");
             FileChannel ch = raf.getChannel()) {

            long numSectors = fileLen / SECTOR;
            long lastProgressPrint = System.currentTimeMillis();

            long chunkSize = 512L * 1024 * 1024;
            for (long chunkStart = 0; chunkStart < fileLen; chunkStart += chunkSize) {
                long thisChunkSize = Math.min(chunkSize, fileLen - chunkStart);
                MappedByteBuffer map = ch.map(FileChannel.MapMode.READ_ONLY, chunkStart, thisChunkSize);
                map.order(ByteOrder.LITTLE_ENDIAN);

                for (long localSector = 0; localSector * SECTOR < thisChunkSize; localSector++) {
                    long localOff = localSector * SECTOR;
                    long globalOff = chunkStart + localOff;
                    if (localOff + 4 > thisChunkSize) break;

                    if (System.currentTimeMillis() - lastProgressPrint > 5000) {
                        System.out.printf("  ...at %.1f%%  (%d/%d sectors, %d SLZ containers seen so far, %d saved)%n",
                                100.0 * globalOff / fileLen, globalOff / SECTOR, numSectors, slzContainers, slzSaved);
                        lastProgressPrint = System.currentTimeMillis();
                    }

                    byte b0 = map.get((int) localOff);
                    byte b1 = (localOff + 1 < thisChunkSize) ? map.get((int) localOff + 1) : 0;
                    byte b2 = (localOff + 2 < thisChunkSize) ? map.get((int) localOff + 2) : 0;

                    if (b0 == 'S' && b1 == 'L' && b2 == 'Z') {
                        slzContainers++;
                        int avail = (int) Math.min(MAX_CONTAINER, thisChunkSize - localOff);
                        byte[] container = new byte[avail];
                        map.get((int) localOff, container, 0, avail);
                        try {
                            byte[] out = SlzCodec.decompress(container);

                            // Save decompressed data to disk
                            String filename = String.format("slz_decompressed_0x%X.bin", globalOff);
                            File outFile = new File(outputDir, filename);
                            try (FileOutputStream fos = new FileOutputStream(outFile)) {
                                fos.write(out);
                                slzSaved++;
                            } catch (IOException e) {
                                System.err.println("Failed to save decompressed file for SLZ at offset 0x" + Long.toHexString(globalOff) + ": " + e.getMessage());
                            }

                            Hit h = findLongestPrintableRun(out);
                            if (h != null && (h.runLength >= MIN_RUN || h.wordCount >= MIN_WORDS)) {
                                h.fileOffset = globalOff;
                                h.fromSlz = true;
                                h.slzMode = SlzCodec.readMode(container);
                                h.slzOutSize = out.length;
                                slzHits.add(h);
                            }
                        } catch (Exception ex) {
                            slzFailed++;
                        }
                    } else {
                        int avail = (int) Math.min(SECTOR * 4, thisChunkSize - localOff);
                        byte[] raw = new byte[avail];
                        map.get((int) localOff, raw, 0, avail);
                        Hit h = findLongestPrintableRun(raw);
                        if (h != null && (h.runLength >= MIN_RUN || h.wordCount >= MIN_WORDS)) {
                            h.fileOffset = globalOff;
                            h.fromSlz = false;
                            rawHits.add(h);
                        }
                    }
                }
            }
        }

        Collections.sort(rawHits);
        Collections.sort(slzHits);

        try (PrintWriter w = new PrintWriter(new FileWriter("pack_scan_report.txt"))) {
            w.println("SO1PACK.BIN SCAN REPORT");
            w.println("File: " + f.getAbsolutePath());
            w.println("Size: " + fileLen + " bytes");
            w.println("SLZ containers found: " + slzContainers + "   (failed to decompress: " + slzFailed + ")");
            w.println("Decompressed SLZ files saved: " + slzSaved);
            w.println("Raw-text hits: " + rawHits.size() + "   SLZ-decompressed-text hits: " + slzHits.size());
            w.println();
            w.println("=== TOP RAW (uncompressed) TEXT CANDIDATES — longest run ===");
            writeHits(w, rawHits);
            w.println();
            w.println("=== TOP RAW (uncompressed) TABLE-LIKE CANDIDATES — many short words ===");
            List<Hit> rawByWords = new ArrayList<>(rawHits);
            rawByWords.sort(BY_WORD_COUNT);
            writeHits(w, rawByWords);
            w.println();
            w.println("=== TOP SLZ (decompressed) TEXT CANDIDATES — longest run ===");
            writeHits(w, slzHits);
            w.println();
            w.println("=== TOP SLZ (decompressed) TABLE-LIKE CANDIDATES — many short words ===");
            List<Hit> slzByWords = new ArrayList<>(slzHits);
            slzByWords.sort(BY_WORD_COUNT);
            writeHits(w, slzByWords);
        }

        System.out.println();
        System.out.println("Done. Wrote pack_scan_report.txt");
        System.out.println("SLZ containers found: " + slzContainers + "  (failed: " + slzFailed + ", saved: " + slzSaved + ")");
        System.out.println("Raw hits: " + rawHits.size() + "   SLZ hits: " + slzHits.size());
        System.out.println("Decompressed files saved to: " + outputDir.getAbsolutePath());
    }

    static void writeHits(PrintWriter w, List<Hit> hits) {
        int n = Math.min(TOP_N, hits.size());
        for (int i = 0; i < n; i++) {
            Hit h = hits.get(i);
            w.printf("offset=0x%X (%d)  run=%d  words=%d  %s%n",
                    h.fileOffset, h.fileOffset, h.runLength, h.wordCount,
                    h.fromSlz ? ("SLZ mode=" + h.slzMode + " outSize=" + h.slzOutSize) : "RAW");
            w.println("    " + h.sample);
        }
    }

    static Hit findLongestPrintableRun(byte[] data) {
        int bestStart = -1, bestLen = 0;
        int curStart = -1, curLen = 0;
        int wordCount = 0;
        List<int[]> runs = new ArrayList<>();
        for (int i = 0; i < data.length; i++) {
            int v = data[i] & 0xFF;
            boolean printable = (v >= 32 && v < 127);
            if (printable) {
                if (curStart < 0) curStart = i;
                curLen++;
            } else {
                if (curLen > bestLen) { bestLen = curLen; bestStart = curStart; }
                if (curLen >= 3 && curLen <= 20) { wordCount++; runs.add(new int[]{curStart, curLen}); }
                curStart = -1; curLen = 0;
            }
        }
        if (curLen > bestLen) { bestLen = curLen; bestStart = curStart; }
        if (curLen >= 3 && curLen <= 20) { wordCount++; runs.add(new int[]{curStart, curLen}); }
        if (bestStart < 0 && runs.isEmpty()) return null;

        Hit h = new Hit();
        h.wordCount = wordCount;

        if (bestLen >= MIN_RUN) {
            h.runLength = bestLen;
            int sampleLen = Math.min(bestLen, 200);
            h.sample = new String(data, bestStart, sampleLen, java.nio.charset.StandardCharsets.ISO_8859_1)
                    .replace("\r", "\\r").replace("\n", "\\n");
        } else if (!runs.isEmpty()) {
            h.runLength = bestLen;
            StringBuilder sb = new StringBuilder();
            for (int[] r : runs) {
                if (sb.length() > 200) break;
                sb.append(new String(data, r[0], r[1], java.nio.charset.StandardCharsets.ISO_8859_1)).append('|');
            }
            h.sample = sb.toString();
        } else {
            return null;
        }
        return h;
    }
}
