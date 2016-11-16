package com.github.lstephen.ootp.extract.html;

import com.github.lstephen.ootp.extract.html.loader.DiskCachingLoader;
import com.github.lstephen.ootp.extract.html.loader.InMemoryCachedLoader;
import com.github.lstephen.ootp.extract.html.loader.JsoupLoader;
import com.github.lstephen.ootp.extract.html.loader.PageLoader;

/** @author lstephen */
public final class PageFactory {

  private static final PageLoader DEFAULT_PAGE_LOADER =
      InMemoryCachedLoader.wrap(DiskCachingLoader.wrap(new JsoupLoader()));

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
