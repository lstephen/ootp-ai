package com.ljs.ootp.extract.html;

import java.util.concurrent.Callable;
import org.jsoup.nodes.Document;

/**
 *
 * @author lstephen
 */
public final class PageLoaders {

    private PageLoaders() { }

    public static Callable<Document> asCallable(
        final PageLoader loader, final String url) {

        return new Callable<Document>() {
            @Override
            public Document call() {
                return loader.load(url);
            }
        };
    }

}
