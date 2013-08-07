package uk.ac.susx.mlcl.erl.lib;

import com.google.common.collect.ImmutableList;
import com.google.common.io.ByteSource;
import com.google.common.io.ByteStreams;
import com.google.common.io.InputSupplier;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Static utilities to assist with IO operations.
 *
 * @author Hamish Morgan
 */
public class IOUtils {

    /**
     * Joins multiple {@link ByteSource} suppliers into a single source.
     * Streams returned from the supplier will contain the concatenated data from
     * the streams of the underlying suppliers.
     * <p/>
     * Only one underlying input stream will be open at a time. Closing the
     * joined stream will close the open underlying stream.
     * <p/>
     * Reading from the joined stream will throw a {@link NullPointerException}
     * if any of the suppliers are null or return null.
     *
     * @param sources the sources to concatenate
     * @return a source that will return a stream containing the concatenated stream data
     */
    @Nonnull
    public static ByteSource join(@Nonnull final ByteSource... sources) {
        checkNotNull(sources, "sources");
        return new ByteSource() {
            @Override
            public InputStream openStream() throws IOException {
                final ImmutableList.Builder<InputSupplier<InputStream>> listBuilder = ImmutableList.builder();
                for (ByteSource source : sources)
                    listBuilder.add(asInputSupplier(source));
                return ByteStreams.join(listBuilder.build()).getInput();
            }

            @Override
            public long size() throws IOException {
                long size = 0;
                for (ByteSource source : sources)
                    size += source.size();
                return size;
            }
        };
    }

    /**
     * Convert the given {@link ByteSource} to an input supplier.
     *
     * @param source sources to be converted
     * @return input supplier
     */
    @Nonnull
    public static InputSupplier<InputStream> asInputSupplier(@Nonnull final ByteSource source) {
        checkNotNull(source, "source");
        return new InputSupplier<InputStream>() {
            @Override
            public InputStream getInput() throws IOException {
                return source.openStream();
            }
        };
    }


    /**
     * Convert the given {@link ByteSource} such that an input stream is gzip decompressed.
     *
     * @param gzipByteSource
     * @return
     */
    @Nonnull
    public static ByteSource asGzipByteSource(@Nonnull final ByteSource gzipByteSource) {
        return new ByteSource() {
            @Nonnull
            @Override
            public InputStream openStream() throws IOException {
                return new GZIPInputStream(gzipByteSource.openStream());
            }
        };
    }
}
