package uk.ac.susx.mlcl.erl.linker;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * <tt>AliasMappingGenerator</tt> is an implementation of {@link uk.ac.susx.mlcl.erl.linker.CandidateGenerator} that
 * looks up the given query in an alias map. If the key is found then the map value is passed to the encapsulated
 * delegate. Otherwise the query is passed to the delegate.
 * <p/>
 * Optionally the mapping can be set to be recursive. In this mode the query is first looked up in the map, if an alias
 * is found then it becomes the alias and the processes repeats. This continues until either the query is not found,
 * or a loop is detected. If a single terminating query is not found (either in the map or the original query) then
 * it is passed to the delegate. Otherwise, if a loop is detected, then all terms in the compose the loop are
 * passed to the delegate, and the union of these results are returned.
 *
 * @author Hamish Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public class AliasMappingGenerator extends ForwardingGenerator {

    private final Map<String, String> aliasMap;
    private final boolean recusiveMapping;

    public AliasMappingGenerator(final CandidateGenerator delegate, final Map<String, String> aliasMap, boolean recusiveMapping) {
        super(delegate);
        this.aliasMap = checkNotNull(aliasMap, "aliasMap");
        this.recusiveMapping = recusiveMapping;
    }

    @Override
    public Set<String> findCandidates(final String mention) throws IOException {
        if (recusiveMapping) {
            final Deque<String> seen = new ArrayDeque<String>();
            String query = mention;

            // Repeatedly map the query to it's alias, until either it isn't found or a loop is detected.
            // All discovered aliases are stored in the seen stack, starting with the original mention
            seen.push(query);
            boolean loopDetected = false;
            while (!loopDetected && aliasMap.containsKey(query)) {
                query = aliasMap.get(query);
                if (seen.contains(query)) {
                    loopDetected = true;
                } else {
                    seen.push(query);
                }
            }

            // Pop elements of the stack until the terminating query is discovered
            Set<String> result = Collections.EMPTY_SET;
            while (!seen.isEmpty() && !seen.peek().equals(query))
                result = Sets.union(result, super.findCandidates(seen.pop()));
            result = Sets.union(result, super.findCandidates(query));
            return result;
        } else {
            return aliasMap.containsKey(mention)
                    ? Collections.singleton(aliasMap.get(mention))
                    : super.findCandidates(mention);
        }
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
