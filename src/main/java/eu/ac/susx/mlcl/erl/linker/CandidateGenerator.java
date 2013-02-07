/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.ac.susx.mlcl.erl.linker;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

/**
 *
 * @author hiam20
 */
public interface CandidateGenerator {

    /**
     *
     *
     * <p/>
     * @param mention Plain text mention string
     * @return List of candidate entity id's matching the given mention
     * @throws IOException
     */
    List<String> findCandidates(String mention) throws IOException;

    /**
     * Query the knowledge base with the given list of plain text string, as a single batch
     * operation, returning a map from each query to a list of matching id's.
     *
     * @param queries query strings
     * @return map from queries to a list of result id strings
     * @throws IOException
     * @throws ExecutionException
     */
    Map<String, List<String>> batchFindCandidates(Set<String> queries)
            throws IOException, ExecutionException;
}
