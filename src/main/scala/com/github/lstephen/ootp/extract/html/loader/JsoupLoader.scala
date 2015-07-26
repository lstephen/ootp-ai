package com.github.lstephen.ootp.extract.html.loader

import java.io.InputStream

import java.net.URL

import com.google.common.base.Charsets

import com.typesafe.scalalogging.LazyLogging

import org.jsoup.Jsoup
import org.jsoup.nodes.Document

import resource._

class JsoupLoader extends PageLoader with LazyLogging  {
  override def load(url: String) : Document = {
    logger.info(s"Loading page $url...")

    val d = managed(new URL(url).openStream()) map { load _ }

    d.opt getOrElse { throw new PageLoaderException }
  }

  def load(is: InputStream) : Document = {
    Jsoup.parse(is, Charsets.ISO_8859_1.name(), "");
  }
}

