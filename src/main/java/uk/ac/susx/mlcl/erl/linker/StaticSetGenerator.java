package uk.ac.susx.mlcl.erl.linker;

import com.google.common.collect.Sets;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * <tt>StaticSetGenerator</tt> is an implementation of {@link CandidateGenerator} which simply checks if the given
 * query mention is contained within a given set of strings. If the string is found then the a single candidate is
 * returned, that of the mention surface text. If the string is not found then the empty result is returned.
 *
 * @author Hamish Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public class StaticSetGenerator extends NilGenerator {

    private final Set<String> strings;

    /**
     * @param strings
     */
    public StaticSetGenerator(final Set<String> strings) {
        super();
        this.strings = checkNotNull(strings, "strings");
    }


    /**
     * Convenience constructor that shallow copies an arbitrary iterable collection.
     *
     * @param strings
     */
    public StaticSetGenerator(final Iterable<String> strings) {
        this(Sets.newHashSet(strings));
    }


    @Override
    public Set<String> findCandidates(String mention) throws IOException {
        return strings.contains(mention)
                ? Collections.singleton(mention)
                : super.findCandidates(mention);
    }

}
