/*
 * Copyright (c) 2010, Hamish Morgan.
 * All Rights Reserved.
 */
package uk.ac.susx.mlcl.erl.kb;

import com.google.common.base.Throwables;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.Weigher;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.NotThreadSafe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Not thread safe
 *
 * @author hamish
 */
@Nonnull
@NotThreadSafe
public class CachedKnowledgeBase implements KnowledgeBase {

    private static final Logger LOG =
	    LoggerFactory.getLogger(CachedKnowledgeBase.class);

    private final LoadingCache<String, String> textCache;

    private final LoadingCache<String, List<String>> searchCache;

    CachedKnowledgeBase(LoadingCache<String, String> textCache,
			LoadingCache<String, List<String>> searchCache) {
	this.textCache = textCache;
	this.searchCache = searchCache;
    }

    public List<String> search(final String query) throws IOException {
	try {
	    return searchCache.get(query);
	} catch (ExecutionException ex) {
	    Throwables.propagateIfInstanceOf(ex.getCause(), IOException.class);
	    Throwables.propagateIfPossible(ex.getCause());
	    throw new RuntimeException("unexpected: ", ex);
	}
    }

    public String text(final String id) throws IOException {
	try {
	    return textCache.get(id);
	} catch (ExecutionException ex) {
	    Throwables.propagateIfInstanceOf(ex.getCause(), IOException.class);
	    Throwables.propagateIfPossible(ex.getCause());
	    throw new RuntimeException("unexpected: ", ex);
	}
    }

    public static KnowledgeBase wrap(final KnowledgeBase inner) {
	if (inner instanceof CachedKnowledgeBase)
	    return inner;

	final Weigher<String, String> textWeighter =
		new Weigher<String, String>() {
		    public int weigh(String key, String value) {
			return (key.length() + value.length()) * 2;

		    }
		};
	final CacheLoader<String, String> textLoader =
		new CacheLoader<String, String>() {
		    @Override
		    public String load(String key) throws Exception {
			return inner.text(key);
		    }
		};

	final LoadingCache<String, String> textCache = CacheBuilder.newBuilder()
		.weigher(textWeighter)
		.maximumWeight(1 << 20)
		.build(textLoader);



	final Weigher<String, List<String>> searchWeighter =
		new Weigher<String, List<String>>() {
		    public int weigh(String key, List<String> values) {
			int sum = key.length();
			for (String value : values)
			    sum += value.length();
			return sum * 2;

		    }
		};
	final CacheLoader<String, List<String>> searchLoader =
		new CacheLoader<String, List<String>>() {
		    @Override
		    public List<String> load(String key) throws Exception {
			return inner.search(key);
		    }
		};

	final LoadingCache<String, List<String>> searchCache = CacheBuilder
		.newBuilder()
		.weigher(searchWeighter)
		.maximumWeight(1 << 20)
		.build(searchLoader);

	return new CachedKnowledgeBase(textCache, searchCache);

    }
}
