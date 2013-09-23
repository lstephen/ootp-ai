package com.ljs.scratch.ootp.html.page;

import org.jsoup.nodes.Document;

/**
 *
 * @author lstephen
 */
public final class UrlLoadingPage implements Page {

    private final String url;

    private final PageLoader loader;

    private UrlLoadingPage(String url, PageLoader loader) {
        this.url = url;
        this.loader = loader;
    }

    @Override
    public Document load() {
        return loader.load(url);
    }

    public static Loading using(final PageLoader loader) {
        return new Loading() {
            @Override
            public UrlLoadingPage loading(String url) {
                return new UrlLoadingPage(url, loader);
            }
        };
    }

    public interface Loading {
        UrlLoadingPage loading(String url);
    }

}
