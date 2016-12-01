package com.github.lstephen.ootp.ai.report

import com.github.lstephen.ootp.ai.io.Printable
import com.github.lstephen.ootp.ai.player.Player
import com.github.lstephen.ootp.ai.player.PlayerId
import com.github.lstephen.ootp.ai.regression.Predictor
import com.github.lstephen.ootp.ai.site.Site
import com.github.lstephen.ootp.ai.score.Scoreable
import com.github.lstephen.ootp.ai.stats.BattingStats
import com.github.lstephen.ootp.ai.stats.History
import com.github.lstephen.ootp.ai.stats.TeamStats
import com.github.lstephen.ootp.ai.value.NowValue
import java.io.PrintWriter
import org.apache.commons.lang3.StringUtils
import scala.collection.JavaConverters._

class DevelopmentReport(site: Site, implicit val predictor: Predictor) extends Printable {

  def playerIdSet(s: TeamStats[_]) = s.getAllRatings.asScala.toSet.map((p: Player) => p.getId)

  val cHitting = site.getTeamBatting
  val pHitting = History.create.loadBatting(site, -1)
  val idsHitting = playerIdSet(cHitting).intersect(playerIdSet(pHitting))

  val cPitching = site.getTeamPitching
  val pPitching = History.create.loadPitching(site, -1)
  val idsPitching = playerIdSet(cPitching).intersect(playerIdSet(pPitching))

  def print(w: PrintWriter): Unit = {

    val dHitting = idsHitting.map(new PlayerDevelopment(_, pHitting, cHitting)).toList.sortBy(pd => (pd.score, - pd.toP.getAge())).reverse
    val dPitching = idsPitching.map(new PlayerDevelopment(_, pPitching, cPitching)).toList.sortBy(pd => (pd.score, -pd.toP.getAge())).reverse

    w.println("Hitters")
    dHitting.filter(_.toP.isHitter).map(_.format).foreach(w.println(_))

    w.format("%nPitchers%n")
    dPitching.filter(_.toP.isPitcher).map(_.format).foreach(w.println(_))
  }
}

class PlayerDevelopment(pid: PlayerId, from: TeamStats[_], to: TeamStats[_])(implicit predictor: Predictor) extends Scoreable {

  val fromP = from.getPlayer(pid)
  val toP = to.getPlayer(pid)

  val fromV = NowValue(fromP)
  val toV = NowValue(toP)

  val score = toV.score - fromV.score

  val format =
      f"${toP.getListedPosition.or("")}%2s ${StringUtils.abbreviate(toP.getName(), 25)}%-25s | ${fromP.getAge}%2s ${fromV.format}%s | ${toP.getAge}%2s ${toV.format}%s | ${score.toLong}%+3d"
}

