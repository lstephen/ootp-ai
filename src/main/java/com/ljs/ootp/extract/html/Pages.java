package com.ljs.ootp.extract.html;

import java.util.concurrent.Callable;
import org.jsoup.nodes.Document;

/**
 *
 * @author lstephen
 */
public final class Pages {

    private Pages() { }

    public static Callable<Document> asCallable(final Page page) {
        return new Callable<Document>() {
            @Override
            public Document call() {
                return page.load();
            }
        };
    }

}
