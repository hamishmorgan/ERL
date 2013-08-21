package uk.ac.susx.mlcl.erl.linker;

import com.google.common.collect.ImmutableMap;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

/**
 * <tt>NilGenerator</tt> is an implementation of {@link CandidateGenerator} which never
 * generates anything. Whenever candidates are requested the empty set is returned.
 *
 * @author Hamish Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public abstract class AbstractGenerator<Q, L> implements CandidateGenerator<Q, L> {

    protected AbstractGenerator() {
    }

    @Nonnull
    @Override
    public abstract Set<L> findCandidates(@Nonnull Q mention) throws IOException;

    @Nonnull
    @Override
    public Map<Q, Set<L>> batchFindCandidates(@Nonnull Iterable<Q> queries)
            throws IOException, ExecutionException {
        ImmutableMap.Builder<Q, Set<L>> mapBuilder = ImmutableMap.builder();
        for (Q query : queries)
            mapBuilder.put(query, findCandidates(query));
        return mapBuilder.build();
    }


}
