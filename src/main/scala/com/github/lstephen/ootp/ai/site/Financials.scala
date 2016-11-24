package com.github.lstephen.ootp.ai.site

import com.github.lstephen.ootp.ai.data.Id
import com.github.lstephen.ootp.ai.roster.Team
import com.github.lstephen.ootp.extract.html.Page

import java.text.NumberFormat

import org.jsoup.nodes.Document

trait Financials {
  def getAvailableForExtensions: Int
  def getAvailableForFreeAgents: Int
}

class FinancialsReport(site: Site, team: Id[Team]) extends Financials {
  val page: Page = site getPage s"team${team.get}fi.html"
  val doc: Document = page.load

  //NumberFormat.getNumberInstance().parse(salary.substring(1))
  val getAvailableForExtensions =
    NumberFormat
      .getNumberInstance()
      .parse(doc.select("td:contains(Extensions) + td").text.substring(1))
      .intValue

  val getAvailableForFreeAgents =
    NumberFormat
      .getNumberInstance()
      .parse(doc.select("td:contains(Free Agents) + td").text.substring(1))
      .intValue
}
