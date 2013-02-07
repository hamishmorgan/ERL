/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.ac.susx.mlcl.erl.linker;

import com.google.api.services.freebase.Freebase2;
import static com.google.common.base.Preconditions.*;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

/**
 *
 * @author hiam20
 */
@Immutable
@Nonnull
public class FreebaseSearchCandidateGenerator implements CandidateGenerator {

    private final Freebase2 freebase;

    public FreebaseSearchCandidateGenerator(Freebase2 freebase) {
        checkNotNull(freebase, "freebase");
        this.freebase = freebase;
    }

    @Override
    public List<String> findCandidates(String mention)
            throws IOException {
        checkNotNull(mention, "mention");
        return freebase.searchGetIds(mention);
    }

    @Override
    public Map<String, List<String>> batchFindCandidates(Set<String> mentions)
            throws IOException, ExecutionException {
        checkNotNull(mentions, "mentions");
        return freebase.batchSearchGetIds(mentions);
    }
}
