/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.susx.mlcl.erl.linker;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

/**
 *
 * @author Hamish Morgan
 * @param <Q> Query type
 * @param <L> Candidate link type
 */
public interface CandidateGenerator<Q,L> {

    /**
     * <p/>
     *
     * @param mention Plain text mention string
     * @return List of candidate entity id's matching the given mention
     * @throws IOException
     */
    @Nonnull
    Set<L> findCandidates(@Nonnull Q mention) throws IOException;

    /**
     * Query the knowledge base with the given list of plain text string, as a single batch
     * operation, returning a map from each query to a list of matching id's.
     *
     * @param queries query strings
     * @return map from queries to a list of result id strings
     * @throws IOException
     * @throws ExecutionException
     */
    @Nonnull
    Map<Q, Set<L>> batchFindCandidates(@Nonnull Set<Q> queries)
            throws IOException, ExecutionException;
}
