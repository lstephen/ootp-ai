package com.github.lstephen.ootp.extract.html.loader;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.reactivex.Maybe;
import io.reactivex.Single;
import org.jsoup.nodes.Document;

/** @author lstephen */
public final class InMemoryCachedLoader implements PageLoader {

  private static final Integer MAXIMUM_CACHE_SIZE = 200;

  private final Cache<String, Document> cache =
      Caffeine.newBuilder()
          .initialCapacity(MAXIMUM_CACHE_SIZE)
          .maximumSize(MAXIMUM_CACHE_SIZE)
          .build();

  private final PageLoader wrapped;

  private InMemoryCachedLoader(final PageLoader wrapped) {
    this.wrapped = wrapped;
  }

  @Override
  public Single<Document> load(String url) {
    Maybe<Document> getCached = Maybe.fromCallable(() -> cache.getIfPresent(url));

    Single<Document> getWrapped = wrapped.load(url).doOnSuccess(d -> cache.put(url, d));

    return getCached.switchIfEmpty(getWrapped);
  }

  public static InMemoryCachedLoader wrap(PageLoader wrapped) {
    return new InMemoryCachedLoader(wrapped);
  }
}
