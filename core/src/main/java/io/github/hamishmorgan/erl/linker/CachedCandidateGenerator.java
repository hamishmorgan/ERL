/*
 * Copyright (c) 2010, Hamish Morgan.
 * All Rights Reserved.
 */
package io.github.hamishmorgan.erl.linker;

import com.google.common.base.Throwables;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.Weigher;
import com.google.common.collect.Sets;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.NotThreadSafe;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Instances of
 * <code>CachedCandidateGenerator</code> adapts another CachedCandidate implementation, and
 * implement a transparent memory cache over the methods.
 * <p/>
 * Repeated attempts to retrieve the same data will be returned from memory, rather than querying
 * the KB service. This particularly useful for web-based services such as Freebase, where
 * unnecessary calls can have a significant effect on performance.
 * <p/>
 * Memory usage is constrained to approximately 1 MiB per cache. There are currently two caches (one
 * for text and the other for search queries) so memory usage should not exceed 2MiB per instance.
 * <p/>
 * Not thread safe
 *
 * @author hamish
 */
@Nonnull
@NotThreadSafe
public class CachedCandidateGenerator<Q,L> implements CandidateGenerator<Q,L> {

    private static final Logger LOG = LoggerFactory.getLogger(CachedCandidateGenerator.class);
    private final LoadingCache<Q, Set<L>> searchCache;

    /**
     * Protected dependency injection constructor. Use
     * {@link CachedCandidateGenerator#wrap(CandidateGenerator)} } instead.
     *
     * @param searchCache
     */
    protected CachedCandidateGenerator(final LoadingCache<Q, Set<L>> searchCache) {
        this.searchCache = checkNotNull(searchCache, "searchCache");
    }

    @Override
    public Set<L> findCandidates(final Q query) throws IOException {
        return get(searchCache, query);
    }

    @Override
    public Map<Q, Set<L>> batchFindCandidates(Iterable<Q> queries)
            throws IOException, ExecutionException {
        return searchCache.getAll(queries);
    }

    protected static <K, V> V get(@Nonnull final LoadingCache<K, V> cache, final K key)
            throws IOException {
        try {
            return cache.get(checkNotNull(key, "key"));
        } catch (@Nonnull final ExecutionException ex) {
            Throwables.propagateIfInstanceOf(ex.getCause(), IOException.class);
            Throwables.propagateIfPossible(ex.getCause());
            throw new RuntimeException("unexpected: ", ex);
        }
    }

    @Nonnull
    public static <Q,L> CandidateGenerator<Q,L> wrap(@Nonnull final CandidateGenerator<Q,L> inner) {
        if (checkNotNull(inner, "inner") instanceof CachedCandidateGenerator) {
            LOG.warn("Ignoring attempt to cache wrap a KnowledgeBase that was already cached.");
            return inner;
        }

        final CacheLoader<Q, Set<L>> searchLoader =
                new CacheLoader<Q, Set<L>>() {
                    @Nullable
                    @Override
                    public Set<L> load(Q key) throws Exception {
                        return inner.findCandidates(key);
                    }

                    @Override
                    public Map<Q, Set<L>> loadAll(Iterable<? extends Q> keys)
                            throws Exception {
                        return inner.batchFindCandidates(Sets.newHashSet(keys));
                    }
                };

        final Weigher<Q, Set<L>> searchWeighter =
                    new Weigher<Q, Set<L>>() {
                        public int weigh(@Nonnull Q key, @Nonnull Set<L> values) {
                            return 1 + values.size();

                        }
                    };

        final LoadingCache<Q, Set<L>> searchCache  = CacheBuilder
                .newBuilder()
                .weigher(searchWeighter)
                .maximumWeight(1 << 16)
                .build(searchLoader);

        return new CachedCandidateGenerator<Q,L>(searchCache);

    }
}
