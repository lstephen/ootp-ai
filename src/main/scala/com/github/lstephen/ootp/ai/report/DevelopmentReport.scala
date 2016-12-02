package com.github.lstephen.ootp.ai.report

import com.github.lstephen.ootp.ai.io.Printable
import com.github.lstephen.ootp.ai.player.Player
import com.github.lstephen.ootp.ai.player.PlayerId
import com.github.lstephen.ootp.ai.regression.{Predictor, Regressable}
import com.github.lstephen.ootp.ai.site.Site
import com.github.lstephen.ootp.ai.score.Scoreable
import com.github.lstephen.ootp.ai.splits.Splits
import com.github.lstephen.ootp.ai.stats.{History, TeamStats}
import com.github.lstephen.ootp.ai.value.NowAbility
import java.io.PrintWriter
import org.apache.commons.lang3.StringUtils
import scala.collection.JavaConverters._

class DevelopmentReport(site: Site, implicit val predictor: Predictor) extends Printable {
  import Regressable._

  def print[T: Regressable](w: PrintWriter, pds: Seq[PlayerDevelopment], filter: PlayerDevelopment => Boolean, regressedOn: Player => Splits[T]) = {
    val r = implicitly[Regressable[T]]
    w.println(r.features.mkString(", "))
    pds.sortBy(pd => (pd.score, -pd.toP.getAge)).reverse.filter(filter).map(_.format(regressedOn)).foreach(w.println(_))
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
    print(w, dHitting, _.toP.isHitter, _.getBattingRatings)

    w.format("%nPitchers%n")
    print(w, dPitching, _.toP.isPitcher, _.getPitchingRatings)
  }
}

class PlayerDevelopment(pid: PlayerId, from: (TeamStats[_], Predictor), to: (TeamStats[_], Predictor)) extends Scoreable {

  val fromP = from._1.getPlayer(pid)
  val toP = to._1.getPlayer(pid)

  val fromV = NowAbility(fromP)(from._2)
  val toV = NowAbility(toP)(to._2)

  val score = toV.score - fromV.score

  def format[T: Regressable](regressedOn: Player => Splits[T]) = {
    def ratingChange(f: Option[Double], t: Option[Double]): Option[Double] =
      for { fr <- f; tr <- t } yield tr - fr

    def formatRatingsChanges(from: T, to: T): String = {
      val r = implicitly[Regressable[T]]

      (r.toInput(from).toOptionList, r.toInput(to).toOptionList)
        .zipped
        .map { case (f, t) => ratingChange(f, t) }
        .map(_.map(v => f"${v.round}%+3d").getOrElse("   "))
        .mkString("")
    }

    val info = f"${toP.getListedPosition.or("")}%2s ${StringUtils.abbreviate(toP.getName(), 25)}%-25s ${toP.getAge}%2s"
    val fromAndTo = f"${fromV.format}%s | ${toV.format}"

    val vsL = formatRatingsChanges(regressedOn(fromP).getVsLeft, regressedOn(toP).getVsLeft)
    val vsR = formatRatingsChanges(regressedOn(fromP).getVsRight, regressedOn(toP).getVsRight)

    f"$info | $fromAndTo | ${score.toLong}%+3d | ${vsL} : ${vsR} |"
  }
}

object PlayerDevelopment {

  private def playerIdSet(s: TeamStats[_]) = s.getAllRatings.asScala.toSet.map((p: Player) => p.getId)

  def between(from: (TeamStats[_], Predictor), to: (TeamStats[_], Predictor)): Seq[PlayerDevelopment] = {
    val pids = playerIdSet(from._1).intersect(playerIdSet(to._1))

    pids.map(new PlayerDevelopment(_, from, to)).toList
  }

}

