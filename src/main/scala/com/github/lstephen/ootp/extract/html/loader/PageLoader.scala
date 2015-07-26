package com.github.lstephen.ootp.extract.html.loader

import org.jsoup.nodes.Document

trait PageLoader {
  def load(url: String) : Document
}

class PageLoaderException(msg: String = null, cause: Throwable = null)
  extends RuntimeException(msg, cause)

