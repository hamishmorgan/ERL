/*
 * Copyright (c) 2010, Hamish Morgan.
 * All Rights Reserved.
 */
package uk.ac.susx.mlcl.erl.kb;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import javax.annotation.Nonnull;

/**
 * A ridiculously simple interface that defines all the required functionality of any attached
 * knowledge base system.
 * <p/>
 * @author Hamish Morgan
 */
@Nonnull
public interface KnowledgeBase {

    /**
     * Query the knowledge base with the given plain text string, returning a list of matching id's.
     * <p/>
     * @param query Plain text query string
     * @return List of entity id's matching the given query
     * @throws IOException
     */
    List<String> search(String query) throws IOException;

    /**
     * Query the knowledge base with the given list of plain text string, as a single batch
     * operation, returning a map from each query to a list of matching id's.
     *
     * @param queries query strings
     * @return map from queries to a list of result id strings
     * @throws IOException
     * @throws ExecutionException
     */
    Map<String, List<String>> batchSearch(Set<String> queries)
            throws IOException, ExecutionException;

    /**
     * For the given id, retrieve an associated plain text document, which describes the entry.
     * <p/>
     * @param id entity identifier
     * @return entity descriptive text
     * @throws IOException
     */
    String text(String id) throws IOException;
}
