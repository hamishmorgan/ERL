package uk.ac.susx.mlcl.erl.linker;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

/**
 * <tt>NilGenerator</tt> is an implementation of {@link uk.ac.susx.mlcl.erl.linker.CandidateGenerator} which never
 * generates anything. Whenever candidates are requested the empty set is returned.
 *
 * @author Hamish Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public class NilGenerator<Q, L> extends AbstractGenerator<Q, L> {

    protected NilGenerator() {
    }

    @Nonnull
    public static <Q, L> NilGenerator<Q, L> newInstance() {
        return new NilGenerator<Q, L>();
    }

    @Nonnull
    @Override
    public Set<L> findCandidates(@Nonnull Q mention) throws IOException {
        return ImmutableSet.of();
    }

}
