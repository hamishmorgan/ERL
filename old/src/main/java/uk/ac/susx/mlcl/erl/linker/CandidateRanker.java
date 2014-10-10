/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.susx.mlcl.erl.linker;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.List;

/**
 * A <tt>CandidateRanker</tt> takes a sequence of candidate knowledgebase links and a query, returning the links
 * ordered by how prausible they are for the given query.
 *
 * @param <Q> query type
 * @param <L> link type
 * @author Hamish Morgan
 */
public interface CandidateRanker<Q, L> {

    /**
     * Takes a sequence of candidate knowledgebase links and a query, returning the links
     * ordered by how prausible they are for the given query.
     *
     * @param query
     * @param candidates
     * @return
     * @throws IOException
     */
    @Nonnull
    List<L> rankCandidates(@Nonnull Q query, @Nonnull Iterable<L> candidates) throws IOException;

}
