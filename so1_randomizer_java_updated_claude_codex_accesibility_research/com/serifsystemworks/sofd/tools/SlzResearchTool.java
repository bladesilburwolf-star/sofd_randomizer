package com.serifsystemworks.sofd.tools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Read-only SLZ locator/extractor. Unlike the original sector scanner, it checks
 * every byte, because valid SOFD SLZ containers are not always 0x800-aligned.
 */
public final class SlzResearchTool {
    private static final int HEADER = 0x10;
    private static final int MAX_INPUT = 8 * 1024 * 1024;
    private static final int MAX_OUTPUT = 32 * 1024 * 1024;
    private SlzResearchTool() { }

    public static void main(String[] args) throws Exception {
        if (args.length < 2) { usage(); return; }
        File input = new File(args[1]);
        if (!input.isFile()) throw new IllegalArgumentException("Not a file: " + input.getAbsolutePath());
        if ("locate".equalsIgnoreCase(args[0])) locate(input, args.length > 2 ? number(args[2]) : 0, args.length > 3 ? number(args[3]) : input.length());
        else if ("extract".equalsIgnoreCase(args[0]) && args.length >= 3) extract(input, number(args[2]), args.length > 3 ? new File(args[3]) : defaultOutput(input, number(args[2])));
        else usage();
    }

    private static void locate(File file, long start, long end) throws Exception {
        if (start < 0 || end < start || end > file.length()) throw new IllegalArgumentException("Invalid range");
        RandomAccessFile in = new RandomAccessFile(file, "r");
        int hits = 0;
        try {
            for (long at = start; at + HEADER <= end; at++) {
                in.seek(at);
                if (in.readUnsignedByte() != 'S' || in.readUnsignedByte() != 'L' || in.readUnsignedByte() != 'Z') continue;
                int mode = in.readUnsignedByte();
                byte[] header = new byte[12]; in.readFully(header);
                int outputSize = ByteBuffer.wrap(header, 4, 4).order(ByteOrder.LITTLE_ENDIAN).getInt();
                if (mode <= 3 && outputSize > 0 && outputSize <= MAX_OUTPUT) {
                    System.out.printf("0x%X  mode=%d  output=%d bytes%s%n", at, mode, outputSize, (at & 0x7FF) == 0 ? "  aligned" : "  unaligned");
                    hits++;
                }
            }
        } finally { in.close(); }
        System.out.println("Plausible SLZ containers: " + hits);
    }

    private static void extract(File file, long offset, File output) throws Exception {
        if (offset < 0 || offset + HEADER > file.length()) throw new IllegalArgumentException("Offset outside file");
        int length = (int) Math.min(MAX_INPUT, file.length() - offset);
        byte[] container = new byte[length];
        RandomAccessFile in = new RandomAccessFile(file, "r");
        try { in.seek(offset); in.readFully(container); } finally { in.close(); }
        if (!SlzCodec.isSlzContainer(container)) throw new IllegalArgumentException("No SLZ header at 0x" + Long.toHexString(offset));
        byte[] unpacked = SlzCodec.decompress(container);
        File parent = output.getAbsoluteFile().getParentFile(); if (parent != null) parent.mkdirs();
        FileOutputStream out = new FileOutputStream(output); try { out.write(unpacked); } finally { out.close(); }
        System.out.printf("Extracted 0x%X (mode %d): %d bytes -> %s%n", offset, SlzCodec.readMode(container), unpacked.length, output.getPath());
    }

    private static File defaultOutput(File input, long offset) { return new File("slz_research", input.getName() + String.format("_0x%X.bin", offset)); }
    private static long number(String text) { return text.startsWith("0x") || text.startsWith("0X") ? Long.parseLong(text.substring(2), 16) : Long.parseLong(text); }
    private static void usage() { System.out.println("Read-only SLZ research\n  locate <pack> [start] [end]\n  extract <pack> <offset> [output-file]\nNumbers accept decimal or 0x hexadecimal."); }
}
