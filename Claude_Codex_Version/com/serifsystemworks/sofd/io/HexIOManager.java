package com.serifsystemworks.sofd.io;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class HexIOManager {

    private final File targetFile;

    public HexIOManager(File targetFile) {
        this.targetFile = targetFile;
    }

    // --- READ OPERATIONS ---

    public int readUInt16LE(long offset) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(targetFile, "r")) {
            raf.seek(offset);
            int b1 = raf.readUnsignedByte();
            int b2 = raf.readUnsignedByte();
            return (b2 << 8) | b1;
        }
    }

    public int readInt32LE(long offset) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(targetFile, "r")) {
            raf.seek(offset);
            int b1 = raf.readUnsignedByte();
            int b2 = raf.readUnsignedByte();
            int b3 = raf.readUnsignedByte();
            int b4 = raf.readUnsignedByte();
            return (b4 << 24) | (b3 << 16) | (b2 << 8) | b1;
        }
    }

    public byte[] readBytes(long offset, int length) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(targetFile, "r")) {
            raf.seek(offset);
            byte[] data = new byte[length];
            raf.readFully(data);
            return data;
        }
    }

    // --- WRITE OPERATIONS ---

    public void writeUInt16LE(long offset, int value) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(targetFile, "rw")) {
            raf.seek(offset);
            raf.writeByte(value & 0xFF);
            raf.writeByte((value >> 8) & 0xFF);
        }
    }

    public void writeInt32LE(long offset, int value) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(targetFile, "rw")) {
            raf.seek(offset);
            raf.writeByte(value & 0xFF);
            raf.writeByte((value >> 8) & 0xFF);
            raf.writeByte((value >> 16) & 0xFF);
            raf.writeByte((value >> 24) & 0xFF);
        }
    }

    public void writeBytes(long offset, byte[] data) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(targetFile, "rw")) {
            raf.seek(offset);
            raf.write(data);
        }
    }

    public File getTargetFile() {
        return targetFile;
    }
}