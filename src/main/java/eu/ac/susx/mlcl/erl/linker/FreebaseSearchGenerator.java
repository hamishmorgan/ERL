/*
 * Copyright (c) 2012-2013, Hamish Morgan.
 * All Rights Reserved.
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
 * A candidate link generator that backs off to the Freebase search API.
 * 
 * @author Hamish Morgan
 */
@Immutable
@Nonnull
public class FreebaseSearchGenerator implements CandidateGenerator {

    private final Freebase2 freebase;

    public FreebaseSearchGenerator(Freebase2 freebase) {
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
