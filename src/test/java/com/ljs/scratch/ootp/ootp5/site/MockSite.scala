package com.ljs.scratch.ootp.ootp5.site

import com.ljs.ootp.ai.ootp5.SiteImpl
import com.ljs.ootp.ai.rating.Scale
import com.ljs.ootp.ai.site.Site
import com.ljs.ootp.ai.site.SiteDefinition
import com.ljs.ootp.ai.site.Version
import com.ljs.ootp.extract.html.Page

import org.jsoup.Jsoup
import org.jsoup.nodes.Document

import org.mockito.Mockito._

import org.scalatest.mock.MockitoSugar

class MockSite extends MockitoSugar {

    val site = mock[SiteImpl]

    val page = mock[Page]

    val toMock = site

    when(site.getDefinition).thenReturn(mock[SiteDefinition])

    def name(name: String) = when(site.getName).thenReturn(name)

    def version(v: Version) = when(site.getType).thenReturn(v)
    def `type`(v: Version) = version(v)

    def abilityScale(s: Scale[_]) = when[Scale[_]](site.getAbilityRatingScale).thenReturn(s)
    def potentialScale(s: Scale[_]) = when[Scale[_]](site.getPotentialRatingScale).thenReturn(s)

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

