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

  def print(w: PrintWriter, pds: Seq[PlayerDevelopment], filter: PlayerDevelopment => Boolean) = {
    pds.sortBy(pd => (pd.score, -pd.toP.getAge)).reverse.filter(filter).map(_.format).foreach(w.println(_))
  }

  def print(w: PrintWriter): Unit = {
    val dHitting = PlayerDevelopment.between(site.getTeamBatting, History.create.loadBatting(site, -1))
    val dPitching = PlayerDevelopment.between(site.getTeamPitching, History.create.loadPitching(site, -1))

    w.format("%nHitters%n")
    print(w, dHitting, _.toP.isHitter)

    w.format("%nPitchers%n")
    print(w, dPitching, _.toP.isPitcher)
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

object PlayerDevelopment {

  private def playerIdSet(s: TeamStats[_]) = s.getAllRatings.asScala.toSet.map((p: Player) => p.getId)

  def between(from: TeamStats[_], to: TeamStats[_])(implicit predictor: Predictor): Seq[PlayerDevelopment] = {
    val pids = playerIdSet(from).intersect(playerIdSet(to))

    pids.map(new PlayerDevelopment(_, from, to)).toList
  }

}

