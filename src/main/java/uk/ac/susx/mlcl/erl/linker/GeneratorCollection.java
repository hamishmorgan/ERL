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
public class GeneratorCollection
        extends AbstractCollection<CandidateGenerator>
        implements CandidateGenerator {

    /**
     * Control the way candidates are combined from the child generators.
     */
    public enum AggregationMethod {
        /**
         * The child generators are queries in order. The first non-empty result is returned.
         */
        FIRST {
            Set<String> findCandidates(@Nonnull final List<CandidateGenerator> generators, final String mention)
                    throws IOException {
                for (CandidateGenerator generator : generators) {
                    Set<String> result = generator.findCandidates(mention);
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
            @Nullable
            Set<String> findCandidates(@Nonnull final List<CandidateGenerator> generators, final String mention)
                    throws IOException {
                Set<String> result = null;
                for (CandidateGenerator generator : generators) {
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
            Set<String> findCandidates(@Nonnull final List<CandidateGenerator> generators, final String mention)
                    throws IOException {
                Set<String> result = ImmutableSet.of();
                for (CandidateGenerator generator : generators)
                    result = Sets.union(result, generator.findCandidates(mention));
                return result;
            }
        };

        @Nullable
        abstract Set<String> findCandidates(List<CandidateGenerator> generators, final String mention)
                throws IOException;
    }

    private final List<CandidateGenerator> children;
    private final AggregationMethod aggregationMethod;

    /**
     * Protected dependency injection constructor. You should probably used the builder
     * {@link uk.ac.susx.mlcl.erl.linker.GeneratorCollection#builder()} instead.
     *
     * @param children
     * @param aggregationMethod
     */
    protected GeneratorCollection(final List<CandidateGenerator> children, final AggregationMethod aggregationMethod) {
        this.children = checkNotNull(children, "childrenOf");
        this.aggregationMethod = checkNotNull(aggregationMethod, "aggregationMethod");
    }

    @Nonnull
    public static Builder builder() {
        return new Builder();
    }

    public List<CandidateGenerator> getChildren() {
        return Collections.unmodifiableList(children);
    }

    public AggregationMethod getAggregationMethod() {
        return aggregationMethod;
    }

    @Nullable
    @Override
    public Set<String> findCandidates(final String mention) throws IOException {
        return aggregationMethod.findCandidates(children, mention);
    }

    @Override
    public Map<String, Set<String>> batchFindCandidates(@Nonnull final Set<String> queries)
            throws IOException, ExecutionException {
        ImmutableMap.Builder<String, Set<String>>  mapBuilder = ImmutableMap.builder();
        for (String query : queries)
            mapBuilder.put(query, findCandidates(query));
        return mapBuilder.build();
    }


    @Nonnull
    @Override
    public Iterator<CandidateGenerator> iterator() {
        return children.iterator();
    }

    @Override
    public int size() {
        return children.size();
    }

    public static class Builder {

        public static final AggregationMethod DEFAULT_AGGREGATION_MODE = AggregationMethod.FIRST;
        private final ImmutableList.Builder<CandidateGenerator> children;
        private AggregationMethod aggregationMethod;

        public Builder() {
            children = ImmutableList.builder();
            aggregationMethod = DEFAULT_AGGREGATION_MODE;
        }

        @Nonnull
        public Builder setAggregationMethod(AggregationMethod aggregationMethod) {
            this.aggregationMethod = checkNotNull(aggregationMethod, "aggregationMethod");
            return this;
        }

        @Nonnull
        public Builder addChild(CandidateGenerator generator) {
            children.add(generator);
            return this;
        }

        @Nonnull
        public Builder addChildren(CandidateGenerator... generators) {
            children.add(generators);
            return this;
        }

        @Nonnull
        public Builder addChildren(Iterable<? extends CandidateGenerator> generators) {
            children.addAll(generators);
            return this;
        }

        @Nonnull
        public Builder addChildren(Iterator<? extends CandidateGenerator> generators) {
            children.addAll(generators);
            return this;
        }

        @Nonnull
        public GeneratorCollection build() {
            return new GeneratorCollection(children.build(), aggregationMethod);
        }

    }
}
