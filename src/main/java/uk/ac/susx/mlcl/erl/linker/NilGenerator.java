package uk.ac.susx.mlcl.erl.linker;

import com.google.common.collect.ImmutableMap;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

/**
 * <tt>NilGenerator</tt> is an implementation of {@link uk.ac.susx.mlcl.erl.linker.CandidateGenerator} which never
 * generates anything. Whenever candidates are requested the empty set is returned.
 *
 * @author Hamish Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public class NilGenerator implements CandidateGenerator {

    public NilGenerator() {
    }

    @Override
    public Set<String> findCandidates(String mention) throws IOException {
        return Collections.EMPTY_SET;
    }

    @Override
    public Map<String, Set<String>> batchFindCandidates(Set<String> queries)
            throws IOException, ExecutionException {
        ImmutableMap.Builder mapBuilder = ImmutableMap.builder();
        for (String query : queries)
            mapBuilder.put(query, findCandidates(query));
        return mapBuilder.build();
    }


}
