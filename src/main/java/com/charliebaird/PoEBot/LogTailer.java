package com.charliebaird.PoEBot;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Efficient log "observer" that:
 *  - observe(): marks current end-of-file as the checkpoint (skips existing content).
 *  - getLast(): returns lines appended since last checkpoint.
 *
 * Notes:
 *  - Handles partial last line across calls (buffers it until newline arrives).
 *  - If the file is truncated/rotated to smaller size, it resets checkpoint to 0.
 */
public final class LogTailer {

    private static final int DEFAULT_BUFFER_BYTES = 256 * 1024; // 256 KB

    private LogTailer() {}

    // --- State: last observed byte offset + pending partial line bytes ---
    private static volatile long lastOffset = -1L; // -1 means "uninitialized"
    private static final Object lock = new Object();

    // Buffer for a trailing partial line (bytes) that didn't end with '\n' last time
    private static ByteArray pending = new ByteArray(256);

    // Decoder state (UTF-8 by default; set to something else if needed)
    private static final Charset CHARSET = StandardCharsets.UTF_8;

    private static final Path logFile = Paths.get("C:/Program Files (x86)/Grinding Gear Games/Path of Exile/logs/Client.txt");


    /**
     * Mark current end-of-file as the observation point.
     * This does NOT read existing lines (fast even for 1GB+ files).
     */
    public static void observe() {
        synchronized (lock) {
            long size = 0;
            try {
                size = Files.size(logFile);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            lastOffset = size;
            pending.clear();
        }
    }

    /**
     * Read and return all NEW complete lines added since the last observe()/getLast().
     * If called before observe(), it behaves like observe() first (returns empty).
     */
    public static List<String> getLast() {
        synchronized (lock) {
            long size = 0;
            try {
                size = Files.size(logFile);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            if (lastOffset < 0) {
                // Not observed yet: treat as "start observing now"
                lastOffset = size;
                pending.clear();
                return Collections.emptyList();
            }

            if (size < lastOffset) {
                // File truncated/rotated: reset
                lastOffset = 0L;
                pending.clear();
            }

            long start = lastOffset;
            long bytesToRead = size - start;
            if (bytesToRead <= 0) return Collections.emptyList();

            List<String> out = new ArrayList<>();

            try (FileChannel ch = FileChannel.open(logFile, StandardOpenOption.READ)) {
                ch.position(start);

                ByteBuffer buf = ByteBuffer.allocateDirect(DEFAULT_BUFFER_BYTES);

                // We'll accumulate new data into 'pending' + chunks, and split on '\n'
                while (bytesToRead > 0) {
                    buf.clear();
                    int want = (int) Math.min((long) buf.capacity(), bytesToRead);
                    buf.limit(want);

                    int n = ch.read(buf);
                    if (n <= 0) break;

                    bytesToRead -= n;
                    buf.flip();

                    // Copy from direct buffer into a heap byte[] to parse lines
                    byte[] chunk = new byte[n];
                    buf.get(chunk);

                    // Append chunk to pending, then split on '\n'
                    pending.append(chunk, 0, chunk.length);

                    int lineStart = 0;
                    for (int i = 0; i < pending.size; i++) {
                        if (pending.data[i] == (byte) '\n') {
                            int lineEnd = i; // excludes '\n'
                            // Handle optional '\r' before '\n'
                            int end = (lineEnd > lineStart && pending.data[lineEnd - 1] == (byte) '\r')
                                    ? lineEnd - 1
                                    : lineEnd;

                            String line = decodeUtf8(pending.data, lineStart, end - lineStart);
                            out.add(line);

                            lineStart = i + 1; // next line starts after '\n'
                        }
                    }

                    // Keep leftover (partial) bytes after last '\n' in pending
                    if (lineStart > 0) {
                        pending.compactFrom(lineStart);
                    }
                }

                // Update checkpoint to current EOF
                lastOffset = size;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            return out;
        }
    }

    /**
     * Optional helper: reset internal state (as if never observed).
     */
    public static void reset() {
        synchronized (lock) {
            lastOffset = -1L;
            pending.clear();
        }
    }

    /**
     * Optional helper: get the current stored checkpoint byte offset.
     */
    public static long getCheckpointOffset() {
        return lastOffset;
    }

    // --- decoding ---
    private static String decodeUtf8(byte[] bytes, int off, int len) throws CharacterCodingException {
        // Fast path: UTF-8 decode with replacement disabled to surface real encoding issues.
        CharsetDecoder dec = CHARSET.newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT);

        ByteBuffer bb = ByteBuffer.wrap(bytes, off, len);
        CharBuffer cb = dec.decode(bb);
        return cb.toString();
    }

    // --- tiny growable byte array ---
    private static final class ByteArray {
        byte[] data;
        int size;

        ByteArray(int initial) {
            this.data = new byte[Math.max(1, initial)];
            this.size = 0;
        }

        void clear() { size = 0; }

        void append(byte[] src, int off, int len) {
            ensureCapacity(size + len);
            System.arraycopy(src, off, data, size, len);
            size += len;
        }

        void compactFrom(int from) {
            int remaining = size - from;
            if (remaining > 0) {
                System.arraycopy(data, from, data, 0, remaining);
            }
            size = remaining;
        }

        private void ensureCapacity(int cap) {
            if (cap <= data.length) return;
            int n = data.length;
            while (n < cap) n = n + (n >> 1) + 1; // ~1.5x growth
            byte[] nd = new byte[n];
            System.arraycopy(data, 0, nd, 0, size);
            data = nd;
        }
    }

    public static void printMessages(List<String> messages)
    {
        for (String m : messages)
        {
            System.out.println("\t" + m);
        }
    }
}
