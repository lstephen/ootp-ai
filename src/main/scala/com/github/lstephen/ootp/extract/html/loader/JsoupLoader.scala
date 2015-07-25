package com.github.lstephen.ootp.extract.html.loader

import com.github.lstephen.ootp.extract.html.Documents;
import org.jsoup.nodes.Document;

class JsoupLoader extends PageLoader {
  override def load(url: String) : Document = {
    Documents.load(url)
  }
}

