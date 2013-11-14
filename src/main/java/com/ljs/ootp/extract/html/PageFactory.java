package com.ljs.ootp.extract.html;

/**
 *
 * @author lstephen
 */
public final class PageFactory {

    private static final PageLoader PAGE_LOADER =
        InMemoryCachedLoader.wrap(
            DiskCachingLoader.wrap(
                new JsoupLoader()));

    private PageFactory() { }

    public static Page create(String root, String page) {
        return UrlLoadingPage.using(PAGE_LOADER).loading(root + page);
    }

}
