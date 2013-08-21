/*
 * Copyright (c) 2012-2013, Hamish Morgan.
 * All Rights Reserved.
 */
package uk.ac.susx.mlcl.erl.linker;

import com.google.api.services.freebase.Freebase2;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A candidate link generator that backs off to the Freebase search API.
 *
 * @author Hamish Morgan
 */
@Immutable
public class FreebaseSearchGenerator implements CandidateGenerator<String,String> {

    @Nonnull
    private final Freebase2 freebase;

    public FreebaseSearchGenerator(@Nonnull Freebase2 freebase) {
        checkNotNull(freebase, "freebase");
        this.freebase = freebase;
    }

    @Nonnull
    @Override
    public Set<String> findCandidates(@Nonnull String mention)
            throws IOException {
        checkNotNull(mention, "mention");
        return ImmutableSet.copyOf(freebase.searchGetIds(mention));
    }

    @Nonnull
    @Override
    public Map<String, Set<String>> batchFindCandidates(@Nonnull Set<String> mentions)
            throws IOException, ExecutionException {
        checkNotNull(mentions, "mentions");
        final Map<String, List<String>> ids = freebase.batchSearchGetIds(mentions);
        final ImmutableMap.Builder<String, Set<String>> builder = ImmutableMap.builder();
        for (Map.Entry<String, List<String>> entry : ids.entrySet()) {
            builder.put(entry.getKey(), ImmutableSet.copyOf(entry.getValue()));
        }
        return builder.build();
    }
}
