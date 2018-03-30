package com.github.lstephen.ootp.extract.html.loader;

import java.util.concurrent.Callable;
import org.jsoup.nodes.Document;

/** @author lstephen */
public final class PageLoaders {

  private PageLoaders() {}

  public static Callable<Document> asCallable(final PageLoader loader, final String url) {
    return () -> loader.load(url).blockingGet();
  }
}
