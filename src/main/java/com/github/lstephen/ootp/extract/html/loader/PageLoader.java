package com.github.lstephen.ootp.extract.html.loader;

import org.jsoup.nodes.Document;

public interface PageLoader {
  Document load(String url);
}
