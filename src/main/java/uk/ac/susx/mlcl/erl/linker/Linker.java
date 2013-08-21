package uk.ac.susx.mlcl.erl.linker;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * @param <Q> Query type
 * @param <L> Link type
 */
public interface Linker<Q, L> {

    @Nonnull
    L link(@Nonnull Q query) throws IOException;

    @Nonnull
    Iterable<L> batchLink(@Nonnull Iterable<Q> queries) throws IOException, ExecutionException;


}
