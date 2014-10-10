package uk.ac.susx.mlcl.erl.linker;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import javax.annotation.Nullable;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * <tt>GeneratorCollection</tt> is an implementation of {@link uk.ac.susx.mlcl.erl.linker.CandidateGenerator} that
 * forwards all method invocations to a collection of child generators. Depending on the
 * {@link uk.ac.susx.mlcl.erl.linker.GeneratorCollection.AggregationMethod} the results
 * are aggregated in various ways.
 *
 * @author Hamish Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
@Immutable
public class GeneratorCollection<Q,L>
        extends AbstractCollection<CandidateGenerator<Q,L>>
        implements CandidateGenerator<Q,L> {

    /**
     * Control the way candidates are combined from the child generators.
     */
    public enum AggregationMethod {
        /**
         * The child generators are queries in order. The first non-empty result is returned.
         */
        FIRST {
            @Nonnull
            @Override
            <Q, L> Set<L> findCandidates(@Nonnull final List<CandidateGenerator<Q,L>> generators, @Nonnull final Q mention)
                    throws IOException {
                for (CandidateGenerator<Q,L> generator : generators) {
                    Set<L> result = generator.findCandidates(mention);
                    if (!result.isEmpty())
                        return result;
                }
                return ImmutableSet.of();
            }
        },
        /**
         * All child generators are queries, and those candidates common to all generators are returned.
         */
        INTERSECTION {
            @Nonnull
            @Override
            <Q, L> Set<L> findCandidates(@Nonnull final List<CandidateGenerator<Q,L>> generators, @Nonnull final Q mention)
                    throws IOException {
                Set<L> result = null;
                for (CandidateGenerator<Q,L> generator : generators) {
                    if (result == null)
                        result = generator.findCandidates(mention);
                    else
                        result = Sets.intersection(result, generator.findCandidates(mention));
                    if (result.isEmpty())
                        return ImmutableSet.of();
                }
                if (result == null)
                    result = ImmutableSet.of();
                return result;
            }
        },
        /**
         * All child generators are queries, and all unique those candidates are returned.
         */
        UNION {
            @Nonnull
            @Override
            <Q, L>  Set<L> findCandidates(@Nonnull final List<CandidateGenerator<Q,L>> generators, @Nonnull final Q mention)
                    throws IOException {
                Set<L> result = ImmutableSet.of();
                for (CandidateGenerator<Q,L> generator : generators)
                    result = Sets.union(result, generator.findCandidates(mention));
                return result;
            }

        };

        @Nonnull
        abstract <Q,L> Set<L> findCandidates(@Nonnull List<CandidateGenerator<Q,L>> generators, @Nonnull final Q mention)
                throws IOException;
    }

    @Nonnull
    private final List<CandidateGenerator<Q,L>> children;
    @Nonnull
    private final AggregationMethod aggregationMethod;

    /**
     * Protected dependency injection constructor. You should probably used the builder
     * {@link uk.ac.susx.mlcl.erl.linker.GeneratorCollection#builder()} instead.
     *
     * @param children
     * @param aggregationMethod
     */
    protected GeneratorCollection(@Nonnull final List<CandidateGenerator<Q,L>> children,
                                  @Nonnull final AggregationMethod aggregationMethod) {
        this.children = checkNotNull(children, "childrenOf");
        this.aggregationMethod = checkNotNull(aggregationMethod, "aggregationMethod");
    }

    @Nonnull
    public static Builder builder() {
        return new Builder();
    }

    @Nonnull
    public final List<CandidateGenerator<Q,L>> getChildren() {
        return Collections.unmodifiableList(children);
    }

    @Nonnull
    public final AggregationMethod getAggregationMethod() {
        return aggregationMethod;
    }

    @Nullable
    @Override
    public Set<L> findCandidates(@Nonnull final Q mention) throws IOException {
        return aggregationMethod.findCandidates(children, mention);
    }

    @Nonnull
    @Override
    public Map<Q, Set<L>> batchFindCandidates(@Nonnull final Iterable<Q> queries)
            throws IOException, ExecutionException {
        ImmutableMap.Builder<Q, Set<L>>  mapBuilder = ImmutableMap.builder();
        for (Q query : queries)
            mapBuilder.put(query, findCandidates(query));
        return mapBuilder.build();
    }

    @Nonnull
    @Override
    public final Iterator<CandidateGenerator<Q,L>> iterator() {
        return children.iterator();
    }

    @Override
    public final int size() {
        return children.size();
    }

    public static class Builder<Q,L> {

        public static final AggregationMethod DEFAULT_AGGREGATION_MODE = AggregationMethod.FIRST;
        private final ImmutableList.Builder<CandidateGenerator<Q,L>> children;
        private AggregationMethod aggregationMethod;

        public Builder() {
            children = ImmutableList.builder();
            aggregationMethod = DEFAULT_AGGREGATION_MODE;
        }

        @Nonnull
        public Builder<Q,L> setAggregationMethod(AggregationMethod aggregationMethod) {
            this.aggregationMethod = checkNotNull(aggregationMethod, "aggregationMethod");
            return this;
        }

        @Nonnull
        public Builder<Q,L> addChild(CandidateGenerator<Q,L> generator) {
            children.add(generator);
            return this;
        }

        @Nonnull
        public Builder<Q,L> addChildren(CandidateGenerator<Q,L>... generators) {
            children.add(generators);
            return this;
        }

        @Nonnull
        public Builder<Q,L> addChildren(Iterable<? extends CandidateGenerator<Q,L>> generators) {
            children.addAll(generators);
            return this;
        }

        @Nonnull
        public Builder<Q,L> addChildren(Iterator<? extends CandidateGenerator<Q,L>> generators) {
            children.addAll(generators);
            return this;
        }

        @Nonnull
        public GeneratorCollection<Q,L> build() {
            return new GeneratorCollection<Q,L>(children.build(), aggregationMethod);
        }

    }
}
