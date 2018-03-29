package com.github.lstephen.ootp.extract.html.loader;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import io.reactivex.Single;
import io.reactivex.SingleTransformer;
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
    return Single.just(url).map(URL::new).compose(load()).blockingGet();
  }

  public Document load(URL url) throws IOException {
    return Single.just(url).compose(load()).blockingGet();
  }

  public SingleTransformer<URL, Document> load() {
    return s ->
        s.doOnSuccess(url -> LOG.info("Loading {}...", url))
            .map(
                url -> {
                  try (InputStream in = url.openStream()) {
                    return CharStreams.toString(new InputStreamReader(in, Charsets.ISO_8859_1));
                  }
                })
            .doOnError(t -> LOG.warn("Unable to load page.", t))
            .map(Jsoup::parse);
  }
}
