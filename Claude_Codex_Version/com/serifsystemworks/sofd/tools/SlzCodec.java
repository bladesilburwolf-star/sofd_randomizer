package com.serifsystemworks.sofd.tools;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * SLZ decompressor for tri-Ace's proprietary compression format, used across the
 * "Star Ocean" saga (PSX/PSP/PS2), "Valkyrie Profile" saga, and Radiata Stories.
 *
 * This is a faithful port of slz_triace() from QuickBMS's unz.c
 * (Copyright 2011 CUE, GNU GPL v3 — https://github.com/mistydemeo/quickbms/blob/master/unz.c).
 * VERIFIED 2026-08-11 against real compressed data extracted directly from this project's
 * so1pack.bin (Star Ocean: First Departure US) — confirmed correct output length with no
 * decode errors on multiple real SLZ containers pulled from the actual game archive.
 *
 * SLZ container header (16 bytes):
 *   0x00 : 3 bytes  - signature "SLZ"
 *   0x03 : 1 byte   - mode: 0=STORE, 1=LZSS, 2=LZSS+RLE, 3=LZSS16
 *   0x04 : 4 bytes  - (undocumented/unreliable per original source comment — do not use)
 *   0x08 : 4 bytes  - decompressed (output) size, little-endian — THIS is what the real
 *                     decompressor code actually reads and trusts, despite the original
 *                     comment mislabeling it "compressed length"
 *   0x0C : 4 bytes  - offset to a second embedded file (0 if none)
 *   0x10 : ...      - compressed payload starts here
 */
public final class SlzCodec {

    private SlzCodec() {}

    public static boolean isSlzContainer(byte[] data) {
        return data != null && data.length >= 0x10
                && data[0] == 'S' && data[1] == 'L' && data[2] == 'Z';
    }

    /** Reads the little-endian decompressed-size field straight out of the header (offset 0x08). */
    public static int readDecompressedSize(byte[] container) {
        if (!isSlzContainer(container)) throw new IllegalArgumentException("Not an SLZ container");
        return ByteBuffer.wrap(container, 8, 4).order(ByteOrder.LITTLE_ENDIAN).getInt();
    }

    public static int readMode(byte[] container) {
        if (!isSlzContainer(container)) throw new IllegalArgumentException("Not an SLZ container");
        return container[3] & 0xFF;
    }

    /**
     * Decompresses a full SLZ container (header + payload).
     * Bounds are defensively clamped — the reference implementation has no internal bounds
     * checks and relies on the compressor never overshooting; clamping avoids crashes on
     * truncated/edge-case input without changing behavior on well-formed data.
     */
    public static byte[] decompress(byte[] container) {
        if (!isSlzContainer(container)) throw new IllegalArgumentException("Not an SLZ container");
        int mode = container[3] & 0xFF;
        int outsz = readDecompressedSize(container);
        if (outsz < 0 || outsz > 256_000_000) {
            throw new IllegalArgumentException("Implausible decompressed size: " + outsz);
        }
        byte[] out = new byte[outsz];
        int slz = 0x10;
        int raw = 0;
        int inLen = container.length;

        if (mode == 0) {
            while (raw < outsz && slz < inLen) out[raw++] = container[slz++];
            return out;
        }
        if (mode < 0 || mode > 3) {
            throw new IllegalArgumentException("Unknown SLZ mode: " + mode);
        }

        long flags = 0;
        while (raw < outsz && slz < inLen) {
            if ((flags >>>= 1) <= 0xFFFFL) {
                flags = 0x00FF0000L | (container[slz++] & 0xFF);
                if (mode == 3 && slz < inLen) {
                    flags |= 0xFF000000L | ((long) (container[slz++] & 0xFF) << 8);
                }
            }
            if ((flags & 1) != 0) {
                if (slz >= inLen) break;
                if (raw < outsz) out[raw++] = container[slz++];
                if (mode == 3 && raw < outsz && slz < inLen) out[raw++] = container[slz++];
            } else {
                if (slz + 1 >= inLen) break;
                int pos = container[slz++] & 0xFF;
                int len = container[slz++] & 0xFF;
                if (mode == 2 && len >= 0xF0) {
                    if (len > 0xF0) {
                        len = (len & 0xF) + 3;
                    } else {
                        len = pos + 0x13;
                        if (slz >= inLen) break;
                        pos = container[slz++] & 0xFF;
                    }
                    while (len-- > 0 && raw < outsz) out[raw++] = (byte) pos;
                } else {
                    pos |= (len & 0xF) << 8;
                    len = (len >> 4) + 3;
                    if (mode == 3) {
                        len = (len - 1) << 1;
                        pos <<= 1;
                    }
                    if (pos <= 0 || pos > raw) {
                        // invalid back-reference: corrupt input or wrong offset/mode assumption upstream
                        throw new IllegalStateException("Invalid SLZ back-reference pos=" + pos + " at raw=" + raw);
                    }
                    while (len-- > 0 && raw < outsz) {
                        out[raw] = out[raw - pos];
                        raw++;
                    }
                }
            }
        }
        return out;
    }
}
