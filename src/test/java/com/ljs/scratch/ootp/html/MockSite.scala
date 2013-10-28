package com.ljs.scratch.ootp.html

import com.ljs.scratch.ootp.html.page.Page

import com.ljs.scratch.ootp.site.SiteDefinition
import com.ljs.scratch.ootp.site.Version

import org.easymock.EasyMock.expect

import org.jsoup.Jsoup
import org.jsoup.nodes.Document

import org.powermock.api.easymock.PowerMock._

class MockSite {

    val site = createMock(classOf[Site])

    val page = createMock(classOf[Page])

    expect(site.getDefinition).andStubReturn(createMock(classOf[SiteDefinition]))

    val mock = site

    def name(name: String) = expect(site.getName).andStubReturn(name)

    def version(v: Version) = expect(site.getType).andStubReturn(v)
    def `type`(v: Version) = version(v)

    def expectGetPage(url: String) =
        expect(site.getPage(url)).andStubReturn(page)

    def onLoadPage(url: String) =
        expect(page.load).andStubReturn(loadPage(url))


    private def loadPage(url: String) : Document = {
        val in = getClass.getResourceAsStream(url)

        assume(in != null)

        val doc = Jsoup.parse(in, null, "")

        assume(doc != null)

        doc
    }

}

object MockSite {

    implicit def mockToSite(mock: MockSite) : Site = mock.mock

}

