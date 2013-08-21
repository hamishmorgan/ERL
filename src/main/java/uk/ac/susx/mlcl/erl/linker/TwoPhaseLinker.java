package uk.ac.susx.mlcl.erl.linker;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.List;
import java.util.Set;

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
        final List<L> rankedCandidates = ranker.ranked(query, candidates);
        return rankedCandidates.get(0);
    }
}
