/*
 * Copyright (c) 2012-2013, Hamish Morgan.
 * All Rights Reserved.
 */
package uk.ac.susx.mlcl.erl.linker;

import com.google.common.collect.Lists;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * An implementation of CandidateRanker that orders the candidate list entirely at random -
 * potentially useful baseline.
 *
 * @author Hamish Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
@Nonnull
@Immutable
public final class RandomRanker<Q,L> implements CandidateRanker<Q,L> {

    /**
     * the pseudo-random number generator used for re-ranking
     */
    private final Random random;

    /**
     * Construct a new RandomRanker using the provided pseudo-random number generator. This is
     * especially useful for unit testing, where a fixed seeds makes error reproducible.
     *
     * @param random random number generator
     */
    public RandomRanker(final Random random) {
        this.random = checkNotNull(random, "random");
    }

    /**
     * Construct a new RandomRanker instance using a new pseudo-random number generator.
     */
    public RandomRanker() {
        this(new Random());
    }

    /**
     * Get the pseudo-random number generator associated with this instance.
     *
     * @return the pseudo-random number generator
     */
    public Random getRandom() {
        return random;
    }

    @Override
    public List<L> rankCandidates(Q query, final Iterable<L> candidates) throws IOException {
        // Shallow the input List
        final List<L> result = Lists.newArrayList(checkNotNull(candidates, "candidates"));

        // For every element in the list, swap it with a randomly selected element in the tail 
        // (i.e with an element at an index equal to or greater than the current element.)
        for (int i = 0; i < result.size() - 1; i++) {
            final int j = i + random.nextInt(result.size() - i);
            swap(result, i, j);
        }
        return result;
    }

    /**
     * Swap the elements at indices i and j in the list.
     *
     * @param <T>  generic type of the list
     * @param list list containing elements to be swapped
     * @param i    index of the first element
     * @param j    index of the second element
     * @throws IndexOutOfBoundsException if either index is out of range
     *                                   (<tt>index &lt; 0 || index &gt;= list.size()</tt>)
     */
    private static <T> void swap(@Nonnull final List<T> list, final int i, final int j) {
        if (i != j) {
            final T temp = list.get(i);
            list.set(i, list.get(j));
            list.set(j, temp);
        }
    }
}
