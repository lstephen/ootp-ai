package com.github.lstephen.ootp.extract.html.loader;

import io.reactivex.Single;
import org.jsoup.nodes.Document;

public interface PageLoader {
  Single<Document> load(String url);
}
