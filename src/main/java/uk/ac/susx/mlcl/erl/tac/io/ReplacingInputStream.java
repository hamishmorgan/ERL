package uk.ac.susx.mlcl.erl.tac.io;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * An input stream adapter which searches for particular byte sequences in delegate stream, replacing all matches
 * with the given replacement.
 * <p/>
 * Based on http://stackoverflow.com/a/7743665
 */
class ReplacingInputStream extends FilterInputStream {

    private final CircularByteBuffer inBuffer;
    private final CircularByteBuffer outBuffer;
    private final byte[] search;
    private final byte[] replacement;

    ReplacingInputStream(InputStream delegate, byte[] search, byte[] replacement) {
        super(delegate);
        this.search = search;
        this.replacement = replacement;
        inBuffer = new CircularByteBuffer(search.length);
        outBuffer = new CircularByteBuffer(replacement.length);
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
    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int i = off, value;
        while (i < len && (value = read()) != -1) {
            b[i] = (byte) value;
            i++;
        }
        return i - off;
    }

    @Override
    public long skip(long n) throws IOException {
        return super.skip(n);
    }

    @Override
    public int available() throws IOException {
        return inBuffer.length() + outBuffer.length() + super.available();
    }

    @Override
    public void close() throws IOException {
        super.close();
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


}
