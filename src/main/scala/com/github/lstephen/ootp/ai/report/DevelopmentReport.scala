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
import com.github.lstephen.ootp.ai.value.NowAbility
import java.io.PrintWriter
import org.apache.commons.lang3.StringUtils
import scala.collection.JavaConverters._

class DevelopmentReport(site: Site, implicit val predictor: Predictor) extends Printable {

  def print(w: PrintWriter, pds: Seq[PlayerDevelopment], filter: PlayerDevelopment => Boolean) = {
    pds.sortBy(pd => (pd.score, -pd.toP.getAge)).reverse.filter(filter).map(_.format).foreach(w.println(_))
  }

  def print(w: PrintWriter): Unit = {
    val fromHitting = History.create.loadBatting(site, -1)
    val fromPitching = History.create.loadPitching(site, -1)

    fromHitting.getAllRatings.asScala.foreach(_.setRatingsDefinition(site.getDefinition))
    fromPitching.getAllRatings.asScala.foreach(_.setRatingsDefinition(site.getDefinition))

    val fromPredictions = new Predictor((fromHitting.getAllRatings.asScala ++ fromPitching.getAllRatings.asScala).toSeq.distinct, predictor)

    val dHitting = PlayerDevelopment.between((fromHitting, fromPredictions), (site.getTeamBatting, predictor))
    val dPitching = PlayerDevelopment.between((fromPitching, fromPredictions), (site.getTeamPitching, predictor))

    w.format("%nHitters%n")
    print(w, dHitting, _.toP.isHitter)

    w.format("%nPitchers%n")
    print(w, dPitching, _.toP.isPitcher)
  }
}

class PlayerDevelopment(pid: PlayerId, from: (TeamStats[_], Predictor), to: (TeamStats[_], Predictor)) extends Scoreable {

  val fromP = from._1.getPlayer(pid)
  val toP = to._1.getPlayer(pid)

  val fromV = NowAbility(fromP)(from._2)
  val toV = NowAbility(toP)(to._2)

  val score = toV.score - fromV.score

  val format =
      f"${toP.getListedPosition.or("")}%2s ${StringUtils.abbreviate(toP.getName(), 25)}%-25s | ${fromP.getAge}%2s ${fromV.format}%s | ${toP.getAge}%2s ${toV.format}%s | ${score.toLong}%+3d"
}

object PlayerDevelopment {

  private def playerIdSet(s: TeamStats[_]) = s.getAllRatings.asScala.toSet.map((p: Player) => p.getId)

  def between(from: (TeamStats[_], Predictor), to: (TeamStats[_], Predictor)): Seq[PlayerDevelopment] = {
    val pids = playerIdSet(from._1).intersect(playerIdSet(to._1))

    pids.map(new PlayerDevelopment(_, from, to)).toList
  }

}

