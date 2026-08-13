package com.serifsystemworks.sofd.tools;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Read-only command-line aid for reverse-engineering SOFD binary resources.
 * It deliberately never opens files for writing, so it is safe to use on the
 * only copy of a decompressed SLZ resource or so1pack.bin.
 */
public final class BinaryResearchTool {
    private static final int DEFAULT_CONTEXT = 48;

    private BinaryResearchTool() { }

    public static void main(String[] args) throws Exception {
        if (args.length < 2) { usage(); return; }
        String command = args[0].toLowerCase();
        File file = new File(args[1]);
        if (!file.isFile()) throw new IllegalArgumentException("Not a file: " + file.getAbsolutePath());

        if ("dump".equals(command)) {
            requireArgs(args, 4);
            dump(file, number(args[2]), (int) number(args[3]));
        } else if ("strings".equals(command)) {
            requireArgs(args, 3);
            findAscii(file, args[2], args.length > 3 ? (int) number(args[3]) : DEFAULT_CONTEXT);
        } else if ("refs16".equals(command)) {
            requireArgs(args, 3);
            find16(file, (int) number(args[2]), args.length > 3 ? number(args[3]) : 0,
                    args.length > 4 ? number(args[4]) : file.length());
        } else if ("runs16".equals(command)) {
            requireArgs(args, 4);
            findSmallRuns(file, (int) number(args[2]), (int) number(args[3]),
                    args.length > 4 ? number(args[4]) : 0,
                    args.length > 5 ? number(args[5]) : file.length());
        } else if ("names".equals(command)) {
            requireArgs(args, 5);
            listRecordsWithNames(file, (int) number(args[2]), (int) number(args[3]), (int) number(args[4]),
                    args.length > 5 ? number(args[5]) : 0, args.length > 6 ? number(args[6]) : file.length());
        } else {
            usage();
        }
    }

    private static void dump(File file, long offset, int length) throws Exception {
        checkRange(file, offset, length);
        byte[] bytes = read(file, offset, length);
        System.out.printf("%s: offset 0x%X, %d bytes%n", file.getName(), offset, length);
        for (int base = 0; base < bytes.length; base += 16) {
            StringBuilder hex = new StringBuilder();
            StringBuilder ascii = new StringBuilder();
            for (int i = 0; i < 16; i++) {
                if (i == 8) hex.append(' ');
                if (base + i < bytes.length) {
                    int value = bytes[base + i] & 0xFF;
                    hex.append(String.format("%02X ", value));
                    ascii.append(value >= 32 && value < 127 ? (char) value : '.');
                } else { hex.append("   "); ascii.append(' '); }
            }
            System.out.printf("%08X  %-49s |%s|%n", offset + base, hex, ascii);
        }
        System.out.println("u16 little-endian: " + u16Preview(bytes, 32));
    }

    private static void findAscii(File file, String needle, int context) throws Exception {
        byte[] data = read(file, 0, checkedLength(file.length()));
        byte[] wanted = needle.toLowerCase().getBytes(StandardCharsets.US_ASCII);
        int hits = 0;
        for (int i = 0; i <= data.length - wanted.length; i++) {
            boolean match = true;
            for (int j = 0; j < wanted.length; j++) {
                if (Character.toLowerCase((char) (data[i + j] & 0xFF)) != (char) wanted[j]) { match = false; break; }
            }
            if (match) {
                hits++;
                int start = Math.max(0, i - context);
                int end = Math.min(data.length, i + wanted.length + context);
                System.out.printf("match %d: 0x%X (%d), context 0x%X..0x%X%n", hits, i, i, start, end);
                printCompact(data, start, end);
            }
        }
        System.out.println("Matches: " + hits);
    }

    // Locates every exact little-endian 16-bit value. Useful after an enemy, zone,
    // map, or encounter ID has been identified experimentally.
    private static void find16(File file, int value, long start, long end) throws Exception {
        checkRange(file, start, end - start);
        byte[] data = read(file, start, checkedLength(end - start));
        int hits = 0;
        for (int i = 0; i + 1 < data.length; i++) {
            if (u16(data, i) == value) {
                hits++;
                long at = start + i;
                System.out.printf("match %d: 0x%X (%d)%n", hits, at, at);
                printCompact(data, Math.max(0, i - 24), Math.min(data.length, i + 26));
            }
        }
        System.out.printf("Matches for 0x%04X: %d%n", value & 0xFFFF, hits);
    }

    // Reports contiguous u16 sequences within a plausible ID range. These are only
    // leads, but unlike an ASCII scan they expose encounter-slot / zone-index tables.
    private static void findSmallRuns(File file, int maxValue, int minWords, long start, long end) throws Exception {
        checkRange(file, start, end - start);
        byte[] data = read(file, start, checkedLength(end - start));
        List<Integer> starts = new ArrayList<Integer>();
        for (int alignment = 0; alignment < 2; alignment++) {
            int runStart = -1, runWords = 0;
            for (int pos = alignment; pos + 1 < data.length; pos += 2) {
                int v = u16(data, pos);
                boolean plausible = v > 0 && v <= maxValue;
                if (plausible) {
                    if (runStart < 0) runStart = pos;
                    runWords++;
                } else {
                    if (runWords >= minWords) starts.add(runStart);
                    runStart = -1; runWords = 0;
                }
            }
            if (runWords >= minWords) starts.add(runStart);
        }
        int shown = 0;
        for (Integer pos : starts) {
            if (shown++ >= 500) { System.out.println("Stopped after 500 leads; narrow the range."); break; }
            System.out.printf("candidate: 0x%X (%d)  %s%n", start + pos, start + pos, u16Preview(slice(data, pos, 48), 24));
        }
        System.out.println("Candidate runs: " + starts.size());
    }

    // Lists fixed-size records whose name field is a printable, null-terminated ASCII string.
    // This validates an assumed record layout without changing the file.
    private static void listRecordsWithNames(File file, int stride, int nameOffset, int nameLength, long start, long end) throws Exception {
        if (stride <= 0 || nameOffset < 0 || nameLength <= 0 || nameOffset + nameLength > stride) throw new IllegalArgumentException("Invalid record layout");
        checkRange(file, start, end - start);
        int count = 0;
        RandomAccessFile in = new RandomAccessFile(file, "r");
        try {
            byte[] record = new byte[stride];
            for (long at = start; at + stride <= end; at += stride) {
                in.seek(at); in.readFully(record);
                int nameEnd = 0;
                while (nameEnd < nameLength && record[nameOffset + nameEnd] != 0) nameEnd++;
                if (nameEnd >= 2 && nameEnd < nameLength && isPrintable(record, nameOffset, nameEnd)) {
                    String name = new String(record, nameOffset, nameEnd, StandardCharsets.US_ASCII);
                    System.out.printf("0x%X  %-20s %s%n", at, name, u16Preview(slice(record, 0, Math.min(nameOffset, 32)), 16));
                    count++;
                }
            }
        } finally { in.close(); }
        System.out.println("Named records: " + count);
    }

    private static boolean isPrintable(byte[] data, int start, int length) { for (int i = 0; i < length; i++) if (data[start + i] < 32 || data[start + i] > 126) return false; return true; }
    private static void printCompact(byte[] data, int start, int end) { System.out.println("  hex: " + bytes(data, start, end)); System.out.println(" text: " + printable(data, start, end)); System.out.println(" u16: " + u16Preview(slice(data, start, end - start), 24)); }
    private static String bytes(byte[] data, int start, int end) { StringBuilder s = new StringBuilder(); for (int i = start; i < end; i++) s.append(String.format("%02X ", data[i] & 0xFF)); return s.toString().trim(); }
    private static String printable(byte[] data, int start, int end) { StringBuilder s = new StringBuilder(); for (int i = start; i < end; i++) { int v = data[i] & 0xFF; s.append(v >= 32 && v < 127 ? (char) v : '.'); } return s.toString(); }
    private static String u16Preview(byte[] data, int max) { StringBuilder s = new StringBuilder(); for (int i = 0; i + 1 < data.length && i / 2 < max; i += 2) { if (s.length() > 0) s.append(' '); s.append(String.format("%04X", u16(data, i))); } return s.toString(); }
    private static int u16(byte[] data, int offset) { return (data[offset] & 0xFF) | ((data[offset + 1] & 0xFF) << 8); }
    private static byte[] slice(byte[] data, int start, int length) { int n = Math.max(0, Math.min(length, data.length - start)); byte[] result = new byte[n]; System.arraycopy(data, start, result, 0, n); return result; }
    private static byte[] read(File file, long offset, int length) throws Exception { checkRange(file, offset, length); byte[] data = new byte[length]; RandomAccessFile in = new RandomAccessFile(file, "r"); try { in.seek(offset); in.readFully(data); } finally { in.close(); } return data; }
    private static int checkedLength(long value) { if (value > Integer.MAX_VALUE) throw new IllegalArgumentException("Range is too large; supply a smaller end offset."); return (int) value; }
    private static long number(String text) { return text.startsWith("0x") || text.startsWith("0X") ? Long.parseLong(text.substring(2), 16) : Long.parseLong(text); }
    private static void checkRange(File file, long offset, long length) { if (offset < 0 || length < 0 || offset > file.length() || length > file.length() - offset) throw new IllegalArgumentException("Range is outside " + file.getName()); }
    private static void requireArgs(String[] args, int count) { if (args.length < count) { usage(); throw new IllegalArgumentException("Missing argument"); } }
    private static void usage() { System.out.println("Read-only SOFD binary research tool\nUsage:\n  dump <file> <offset> <length>\n  strings <file> <text> [context-bytes]\n  refs16 <file> <u16-value> [start] [end]\n  runs16 <file> <max-id> <minimum-run-length> [start] [end]\n  names <file> <record-stride> <name-offset> <name-length> [start] [end]\nNumbers accept decimal or 0x hexadecimal."); }
}
