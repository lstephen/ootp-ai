package com.ljs.ootp.extract.html;

import com.ljs.ootp.extract.html.loader.JsoupLoader;
import com.ljs.ootp.extract.html.loader.DiskCachingLoader;
import com.ljs.ootp.extract.html.loader.PageLoader;
import com.ljs.ootp.extract.html.loader.InMemoryCachedLoader;

/**
 *
 * @author lstephen
 */
public final class PageFactory {

    private static final PageLoader DEFAULT_PAGE_LOADER =
        InMemoryCachedLoader.wrap(
            DiskCachingLoader.wrap(
                new JsoupLoader()));

    private final PageLoader loader;

    private PageFactory(PageLoader loader) {
        this.loader = loader;
    }

    public Page getPage(String root, String page) {
        return UrlLoadingPage.using(loader).loading(root + page);
    }

    public static PageFactory create() {
        return create(DEFAULT_PAGE_LOADER);
    }

    public static PageFactory create(PageLoader loader) {
        return new PageFactory(loader);
    }

}
