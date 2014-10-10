package io.github.hamishmorgan.erl.linker;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.collect.ImmutableSet;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * <tt>StaticMapGenerator</tt> is an implementation of {@link CandidateGenerator} which simply checks if the given
 * query mention is contained within a given set of strings. If the string is found then the a single candidate is
 * returned, that of the mention surface text. If the string is not found then the empty result is returned.
 *
 * @author Hamish Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public class StaticMapGenerator<Q, L> extends NilGenerator<Q, L> {

    private final Function<Q, Set<L>> mapping;

    /**
     * @param mapping
     */
    public StaticMapGenerator(final Function<Q, Set<L>> mapping) {
        super();
        this.mapping = checkNotNull(mapping, "mapping");
    }

    public StaticMapGenerator(final Map<Q, Set<L>> mapping) {
        this(Functions.<Q, Set<L>>forMap(mapping, ImmutableSet.<L>of()));
    }

    public static <T> StaticMapGenerator<T, T> inSet(final Set<T> accepted) {
        return new StaticMapGenerator<T, T>(new InSetFunction<T>(accepted));
    }

    @Nonnull
    @Override
    public Set<L> findCandidates(Q mention) throws IOException {
        return mapping.apply(mention);
    }

    private static class InSetFunction<T> implements Function<T, Set<T>> {
        private final Set<T> accepted;

        public InSetFunction(Set<T> accepted) {
            this.accepted = accepted;
        }

        @Nullable
        @Override
        public Set<T> apply(@Nullable T input) {
            return accepted.contains(input) ? ImmutableSet.<T>of(input) : ImmutableSet.<T>of();
        }
    }
}
