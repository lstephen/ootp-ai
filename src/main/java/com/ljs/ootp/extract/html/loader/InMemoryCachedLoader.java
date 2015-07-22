package com.ljs.ootp.extract.html.loader;

import com.google.common.base.Throwables;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.util.concurrent.ExecutionException;
import org.jsoup.nodes.Document;

/**
 *
 * @author lstephen
 */
public final class InMemoryCachedLoader implements PageLoader {

    private static final Integer MAXIMUM_CACHE_SIZE = 200;

    private final LoadingCache<String, Document> cache;

    private InMemoryCachedLoader(final PageLoader wrapped) {
        cache = CacheBuilder
            .newBuilder()
            .maximumSize(MAXIMUM_CACHE_SIZE)
            .initialCapacity(MAXIMUM_CACHE_SIZE)
            .build(new CacheLoader<String, Document>() {
                @Override
                public Document load(String key) {
                    return wrapped.load(key);
                }
            });
    }

    @Override
    public Document load(String url) {
        try {
            return cache.get(url);
        } catch (ExecutionException e) {
            throw Throwables.propagate(e);
        }
    }

    public static InMemoryCachedLoader wrap(PageLoader wrapped) {
        return new InMemoryCachedLoader(wrapped);
    }

}
