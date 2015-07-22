package com.ljs.ootp.extract.html.loader;

/**
 *
 * @author lstephen
 */
public final class PageLoaderBuilder {

    private PageLoader loader = new JsoupLoader();

    private PageLoaderBuilder() { }

    public PageLoaderBuilder inMemoryCache() {
        loader = InMemoryCachedLoader.wrap(loader);
        return this;
    }

    public PageLoaderBuilder diskCache() {
        loader = DiskCachingLoader.wrap(loader);
        return this;
    }

    public PageLoaderBuilder diskCache(String dir) {
        loader = DiskCachingLoader.create(dir, loader);
        return this;
    }

    public PageLoader build() {
        return loader;
    }

    public static PageLoaderBuilder create() {
        return new PageLoaderBuilder();
    }

}
