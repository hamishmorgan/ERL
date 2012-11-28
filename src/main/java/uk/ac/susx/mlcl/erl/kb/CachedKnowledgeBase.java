/*
 * Copyright (c) 2010, Hamish Morgan.
 * All Rights Reserved.
 */
package uk.ac.susx.mlcl.erl.kb;

import static com.google.common.base.Preconditions.*;
import com.google.common.base.Throwables;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.Weigher;
import com.google.common.collect.Sets;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.NotThreadSafe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Instances of
 * <code>CachedKnowledgeBase</code> adapt another KnowledgeBase implementation, and implement a
 * transparent memory cache over the methods.
 *
 * Repeated attempts to retrieve the same data will be returned from memory, rather than querying
 * the KB service. This particularly useful for web-based services such as Freebase, where
 * unnecessary calls can have a significant effect on performance.
 *
 * Memory usage is constrained to approximately 1 MiB per cache. There are currently two caches (one
 * for text and the other for search queries) so memory usage should not exceed 2MiB per instance.
 *
 * Not thread safe
 *
 * @author hamish
 */
@Nonnull
@NotThreadSafe
public class CachedKnowledgeBase implements KnowledgeBase {

    private static final Logger LOG = LoggerFactory.getLogger(CachedKnowledgeBase.class);
    private final LoadingCache<String, String> textCache;
    private final LoadingCache<String, List<String>> searchCache;

    /**
     * Protected dependency injection constructor. Use 
     * {@link CachedKnowledgeBase#wrap(uk.ac.susx.mlcl.erl.kb.KnowledgeBase) } instead.
     *
     * @param textCache
     * @param searchCache
     */
    protected CachedKnowledgeBase(final LoadingCache<String, String> textCache,
                                  final LoadingCache<String, List<String>> searchCache) {
        this.textCache = checkNotNull(textCache);
        this.searchCache = checkNotNull(searchCache);
    }

    @Override
    public List<String> search(final String query) throws IOException {
        return get(searchCache, query);
    }

    @Override
    public String text(final String id) throws IOException {
        return get(textCache, id);
    }

    @Override
    public Map<String, List<String>> batchSearch(Set<String> queries)
            throws IOException, ExecutionException {
        return searchCache.getAll(queries);
    }

    protected static <K, V> V get(final LoadingCache<K, V> cache, final K key)
            throws IOException {
        try {
            return cache.get(checkNotNull(key));
        } catch (final ExecutionException ex) {
            Throwables.propagateIfInstanceOf(ex.getCause(), IOException.class);
            Throwables.propagateIfPossible(ex.getCause());
            throw new RuntimeException("unexpected: ", ex);
        }
    }

    public static KnowledgeBase wrap(final KnowledgeBase inner) {
        if (checkNotNull(inner) instanceof CachedKnowledgeBase) {
            LOG.warn("Ignoring attempt to cache wrap a KnowledgeBase that was already cached.");
            return inner;
        }

        final LoadingCache<String, String> textCache;
        {
            final Weigher<String, String> textWeighter =
                    new Weigher<String, String>() {
                        public int weigh(String key, String value) {
                            return (4 * 2) + (key.length() + value.length()) * 2;

                        }
                    };
            final CacheLoader<String, String> textLoader =
                    new CacheLoader<String, String>() {
                        @Override
                        public String load(String key) throws Exception {
                            return inner.text(key);
                        }
                    };

            textCache = CacheBuilder.newBuilder()
                    .weigher(textWeighter)
                    .maximumWeight(1 << 20)
                    .build(textLoader);
        }

        final LoadingCache<String, List<String>> searchCache;
        {
            final Weigher<String, List<String>> searchWeighter =
                    new Weigher<String, List<String>>() {
                        public int weigh(String key, List<String> values) {
                            int sum = (4 * 2) + (2 * key.length());
                            for (String value : values) {
                                sum += 4 + 2 * value.length();
                            }
                            return sum;

                        }
                    };
            final CacheLoader<String, List<String>> searchLoader =
                    new CacheLoader<String, List<String>>() {
                        @Override
                        public List<String> load(String key) throws Exception {
                            return inner.search(key);
                        }

                        @Override
                        public Map<String, List<String>> loadAll(Iterable<? extends String> keys)
                                throws Exception {
                            return inner.batchSearch(Sets.newHashSet(keys));
                        }
                    };

            searchCache = CacheBuilder
                    .newBuilder()
                    .weigher(searchWeighter)
                    .maximumWeight(1 << 20)
                    .build(searchLoader);
        }

        return new CachedKnowledgeBase(textCache, searchCache);

    }
}
