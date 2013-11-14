package com.ljs.ootp.extract.html;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
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

    @Override
    public String toString() {
        return ReflectionToStringBuilder.reflectionToString(this);
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
