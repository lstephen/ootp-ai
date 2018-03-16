package com.github.lstephen.ootp.extract.html.loader;

import com.google.common.base.Charsets;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.InputStream;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsoupLoader implements PageLoader {
  private static final Logger LOG = LoggerFactory.getLogger(JsoupLoader.class);

  @Override
  public Document load(String url) {
    try {
      return load(new URL(url));
    } catch (IOException e) {
      throw new PageLoaderException("Unable to load " + url, e);
    }
  }

  public Document load(URL url) throws IOException {
    LOG.info("Loading page {}...", url);

    try(InputStream is = url.openStream()) {
      Document d = load(is);

      if (d == null) {
        throw new PageLoaderException("Unable to load " + url);
      }

      return d;
    }
  }

  public Document load(InputStream is) throws IOException {
    return Jsoup.parse(is, Charsets.ISO_8859_1.name(), "");
  }


}
