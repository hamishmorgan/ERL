package io.github.hamishmorgan.erl.linker;

import javax.annotation.Nonnull;

import java.io.IOException;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * <tt>ForwardingGenerator</tt> is an implementation of {@link CandidateGenerator} that forwards all method
 * invocations to another delegated <tt>CandidateGenerator</tt> instance. Consequently it does nothing and is
 * intended as a base for sub-classing.
 *
 * @author Hamish Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public abstract class ForwardingGenerator<Q,L> extends AbstractGenerator<Q,L> {

    @Nonnull
    private final CandidateGenerator delegate;

    /**
     * Protected constructor should only be used by sub-classes.
     *
     * @param delegate inner <tt>CandidateGenerator</tt> instance
     */
    protected ForwardingGenerator(@Nonnull final CandidateGenerator<Q,L> delegate) {
        this.delegate = checkNotNull(delegate, "delegate");
    }

    @Nonnull
    @Override
    public Set<L> findCandidates(@Nonnull final Q mention) throws IOException {
        return delegate.findCandidates(mention);
    }

    /**
     * @return inner <tt>CandidateGenerator</tt> instance
     */
    @Nonnull
    public final CandidateGenerator<Q,L> getDelegate() {
        return delegate;
    }
}
