package io.github.hamishmorgan.erl.linker;

import com.google.common.collect.ImmutableList;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A linker which decomoses the opertation into to stages: First, generate a list of candidate knowedgebase entries.
 * Second order those entries by how likely they are correlate with the query. Finally return the entry with the
 * highest rank.
 *
 * @param <Q> query type
 * @param <L> link type
 * @author Hamish Morgan
 */
public class TwoPhaseLinker<Q, L> implements Linker<Q, L> {

    @Nonnull
    private final CandidateGenerator<Q, L> generator;
    @Nonnull
    private final CandidateRanker<Q, L> ranker;

    public TwoPhaseLinker(@Nonnull CandidateGenerator<Q, L> generator, @Nonnull CandidateRanker<Q, L> ranker) {
        this.generator = checkNotNull(generator, "generator");
        this.ranker = checkNotNull(ranker, "ranker");
    }

    @Override
    @Nonnull
    public L link(@Nonnull Q query) throws IOException {
        final Set<L> candidates = generator.findCandidates(query);
        final List<L> rankedCandidates = ranker.rankCandidates(query, candidates);
        return rankedCandidates.get(0);
    }

    @Nonnull
    @Override
    public Iterable<L> batchLink(@Nonnull Iterable<Q> queries) throws IOException, ExecutionException {
        Map<Q, Set<L>> candidateSets = generator.batchFindCandidates(queries);
        ImmutableList.Builder<L> links = ImmutableList.builder();
        for (Q query : queries) {
            Iterable<L> candidates = candidateSets.get(query);
            List<L> rankedCandidates = ranker.rankCandidates(query, candidates);
            links.add(rankedCandidates.get(0));
        }
        return links.build();
    }
}
