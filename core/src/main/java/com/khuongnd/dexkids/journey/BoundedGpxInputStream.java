package com.khuongnd.dexkids.journey;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

/** Stops an untrusted document provider from feeding unlimited XML into the DOM parser. */
public final class BoundedGpxInputStream extends FilterInputStream {
    public static final int MAX_BYTES = 4 * 1024 * 1024;
    private final long limit;
    private long used;

    public BoundedGpxInputStream(InputStream source) { this(source, MAX_BYTES); }
    public BoundedGpxInputStream(InputStream source, long maxBytes) {
        super(Objects.requireNonNull(source));
        if (maxBytes < 1) throw new IllegalArgumentException("Invalid input limit");
        limit = maxBytes;
    }
    @Override public int read() throws IOException {
        int next = super.read();
        if (next >= 0 && ++used > limit) throw new IOException("GPX file is too large");
        return next;
    }
    @Override public int read(byte[] bytes, int offset, int length) throws IOException {
        Objects.checkFromIndexSize(offset, length, bytes.length);
        if (length == 0) return 0;
        if (used >= limit) {
            int next = super.read();
            if (next >= 0) throw new IOException("GPX file is too large");
            return -1;
        }
        int n = in.read(bytes, offset, (int) Math.min((long) length, limit - used));
        if (n > 0) used += n;
        return n;
    }
}
