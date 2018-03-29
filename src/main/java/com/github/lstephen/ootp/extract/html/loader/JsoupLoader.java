package com.github.lstephen.ootp.extract.html.loader;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import io.reactivex.Single;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsoupLoader implements PageLoader {
  private static final Logger LOG = LoggerFactory.getLogger(JsoupLoader.class);

  @Override
  public Document load(String url) {
    return Single.just(url).map(URL::new).flatMap(this::load).blockingGet();
  }

  public Single<Document> load(URL url) throws IOException {
    return Single.just(url)
        .doOnSuccess(u -> LOG.info("Loading {}...", u))
        .map(
            u -> {
              try (InputStream in = u.openStream()) {
                return CharStreams.toString(new InputStreamReader(in, Charsets.ISO_8859_1));
              }
            })
        .doOnError(t -> LOG.warn("Unable to load page.", t))
        .map(Jsoup::parse);
  }
}
