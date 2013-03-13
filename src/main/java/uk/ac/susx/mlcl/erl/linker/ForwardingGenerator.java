package uk.ac.susx.mlcl.erl.linker;

import static com.google.common.base.Preconditions.*;

import com.google.common.collect.ImmutableMap;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

/**
 * <tt>ForwardingGenerator</tt> is an implementation of {@link CandidateGenerator} that forwards all method
 * invocations to another delegated <tt>CandidateGenerator</tt> instance. Consequently it does nothing and is
 * intended as a base for sub-classing.
 *
 * @author Hamish Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public abstract class ForwardingGenerator implements CandidateGenerator {

    private final CandidateGenerator delegate;

    /**
     * Protected constructor should only be used by sub-classes.
     *
     * @param delegate inner <tt>CandidateGenerator</tt> instance
     */
    protected ForwardingGenerator(final CandidateGenerator delegate) {
        this.delegate = checkNotNull(delegate, "delegate");
    }

    @Override
    public Set<String> findCandidates(final String mention) throws IOException {
        return delegate.findCandidates(mention);
    }

    @Override
    public Map<String, Set<String>> batchFindCandidates(final Set<String> queries)
            throws IOException, ExecutionException {
        return delegate.batchFindCandidates(queries);
    }

    /**
     * @return inner <tt>CandidateGenerator</tt> instance
     */
    public final CandidateGenerator getDelegate() {
        return delegate;
    }
}
