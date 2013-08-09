package uk.ac.susx.mlcl.erl.tac.io;

import com.google.common.base.Objects;
import javax.annotation.Nullable;

import javax.annotation.Nonnull;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * An input stream adapter which searches for particular byte sequences in delegate stream, replacing all matches
 * with the given replacement.
 * <p/>
 * Based on http://stackoverflow.com/a/7743665
 */
public class ReplacingInputStream extends FilterInputStream {

    @Nonnull
    private final CircularByteBuffer inBuffer;
    @Nonnull
    private final CircularByteBuffer outBuffer;
    private final byte[] search;
    private final byte[] replacement;

    public ReplacingInputStream(InputStream delegate, @Nonnull byte[] search, @Nonnull byte[] replacement) {
        super(checkNotNull(delegate, "delegate"));
        this.search = checkNotNull(search, "search");
        this.replacement = checkNotNull(replacement, "replacement");
        inBuffer = new CircularByteBuffer(Math.max(search.length, 1));
        outBuffer = new CircularByteBuffer(Math.max(replacement.length, 1));
    }

    @Override
    public int read() throws IOException {
        if (outBuffer.isEmpty()) {
            // read ahead
            while (inBuffer.length() < search.length) {
                final int nextByte = super.read();
                if (nextByte == -1)
                    break;
                inBuffer.put((byte) nextByte);
            }

            if (inBuffer.equals(search)) {

                inBuffer.clear();
                outBuffer.put(replacement);

            } else if (!inBuffer.isEmpty()) {
                outBuffer.put(inBuffer.get());
            }
        }

        return outBuffer.isEmpty() ? -1 : outBuffer.get();
    }

    @Override
    public int read(@Nonnull byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int i = 0, value;
        while (i < len && (value = read()) != -1) {
            b[i + off] = (byte) value;
            i++;
        }

        return i == 0 && len > 0 ? -1 : i;
//        return i < len ? -1 : i;
    }

    @Override
    public long skip(long n) throws IOException {
        int i = 0;
        while (i < n && -1 != read())
            ++i;
        return i;
    }

    @Override
    public int available() throws IOException {
        return inBuffer.length() + outBuffer.length() + super.available();
    }

    @Override
    public synchronized void mark(int readlimit) {
        throw new UnsupportedOperationException("Mark/reset is not supported.");
    }

    @Override
    public synchronized void reset() throws IOException {
        throw new UnsupportedOperationException("Mark/reset is not supported.");
    }

    @Override
    public boolean markSupported() {
        return false;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("inBuffer", inBuffer)
                .add("outBuffer", outBuffer)
                .add("search", search)
                .add("replacement", replacement)
                .add("markSupported", markSupported())
                .toString();
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ReplacingInputStream that = (ReplacingInputStream) o;

        if (!inBuffer.equals(that.inBuffer)) return false;
        if (!outBuffer.equals(that.outBuffer)) return false;
        if (!Arrays.equals(replacement, that.replacement)) return false;
        if (!Arrays.equals(search, that.search)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = inBuffer.hashCode();
        result = 31 * result + outBuffer.hashCode();
        result = 31 * result + Arrays.hashCode(search);
        result = 31 * result + Arrays.hashCode(replacement);
        return result;
    }
}
