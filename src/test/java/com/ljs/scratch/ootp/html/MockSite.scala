package com.ljs.scratch.ootp.html

import com.ljs.scratch.ootp.html.page.Page

import com.ljs.scratch.ootp.site.SiteDefinition
import com.ljs.scratch.ootp.site.Version

import org.jsoup.Jsoup
import org.jsoup.nodes.Document

import org.mockito.Mockito._

import org.scalatest.mock.MockitoSugar

class MockSite extends MockitoSugar {

    val site = mock[Site]

    val page = mock[Page]

    when(site.getDefinition).thenReturn(mock[SiteDefinition])

    val toMock = site

    def name(name: String) = when(site.getName).thenReturn(name)

    def version(v: Version) = when(site.getType).thenReturn(v)
    def `type`(v: Version) = version(v)

    def expectGetPage(url: String) =
        when(site.getPage(url)).thenReturn(page)

    def onLoadPage(url: String) =
        when(page.load).thenReturn(loadPage(url))


    private def loadPage(url: String) : Document = {
        val in = getClass.getResourceAsStream(url)

        assume(in != null)

        val doc = Jsoup.parse(in, null, "")

        assume(doc != null)

        doc
    }

}

object MockSite {

    implicit def mockToSite(mock: MockSite) : Site = mock.toMock

}

