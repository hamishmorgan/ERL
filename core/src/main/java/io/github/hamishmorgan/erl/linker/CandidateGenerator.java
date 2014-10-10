/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.hamishmorgan.erl.linker;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

/**
 * A <tt>CandidateGenerator</tt> takes a query and produces zero or more candidate links from the knowledge base.
 *
 * @param <Q> Query type
 * @param <L> Candidate link type
 * @author Hamish Morgan
 */
public interface CandidateGenerator<Q, L> {

    /**
     * Get candidate links from the knowledge base for the given query
     *
     * @param query query object
     * @return List of candidate links  matching the given mention
     * @throws IOException if some error occured querying the underlying knowledge base
     */
    @Nonnull
    Set<L> findCandidates(@Nonnull Q query) throws IOException;

    /**
     * Query the knowledge base with the given list of plain text string, as a single batch
     * operation, returning a map from each query to a list of matching id's.
     *
     * @param queries query objects
     * @return map from queries to a list of candidate links
     * @throws IOException        if some error occured querying the underlying knowledge base
     * @throws ExecutionException
     */
    @Nonnull
    Map<Q, Set<L>> batchFindCandidates(@Nonnull Iterable<Q> queries)
            throws IOException, ExecutionException;
}
