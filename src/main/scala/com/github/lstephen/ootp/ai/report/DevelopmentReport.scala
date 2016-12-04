package com.github.lstephen.ootp.ai.report

import com.github.lstephen.ootp.ai.draft.DraftClass
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

class DevelopmentReport(site: Site, implicit val predictor: Predictor)
    extends Printable {
  import Regressable._

  def print[T: Regressable](
      pds: Seq[PlayerDevelopment],
      filter: PlayerDevelopment => Boolean,
      regressedOn: Player => Splits[T])(implicit w: PrintWriter): Unit = {
    val r = implicitly[Regressable[T]]
    w.println(r.features.mkString(", "))
    pds
      .sortBy(pd => (pd.score, -pd.toP.getAge))
      .reverse
      .filter(filter)
      .map(_.format(regressedOn))
      .foreach(w.println(_))
  }

  def print(hitting: Seq[PlayerDevelopment], pitching: Seq[PlayerDevelopment])(
      implicit w: PrintWriter): Unit = {
    w.println("Hitters")
    print(hitting, _.toP.isHitter, _.getBattingRatings)

    w.format("%nPitchers%n")
    print(pitching, _.toP.isPitcher, _.getPitchingRatings)
  }

  def print(w: PrintWriter): Unit = {
    val draft = DraftClass.load(site, site.getDate.getYear)

    val fromHitting = History.create.loadBatting(site, -1)
    val fromPitching = History.create.loadPitching(site, -1)

    fromHitting.getAllRatings.asScala
      .foreach(_.setRatingsDefinition(site.getDefinition))
    fromPitching.getAllRatings.asScala
      .foreach(_.setRatingsDefinition(site.getDefinition))

    val fromPredictions = new Predictor(
      (fromHitting.getAllRatings.asScala ++ fromPitching.getAllRatings.asScala).toSeq.distinct,
      predictor)
    val draftPredictions = new Predictor(draft.getPlayers, predictor)

    val dHitting = PlayerDevelopment.betweenTeamStats(
      (fromHitting, fromPredictions),
      (site.getTeamBatting, predictor))
    val dPitching = PlayerDevelopment.betweenTeamStats(
      (fromPitching, fromPredictions),
      (site.getTeamPitching, predictor))

    val draftHitting = PlayerDevelopment.betweenPlayers(
      (draft.getPlayers.asScala.toSeq, draftPredictions),
      (site.getTeamBatting.getAllRatings.asScala.toSeq, predictor))
    val draftPitching = PlayerDevelopment.betweenPlayers(
      (draft.getPlayers.asScala.toSeq, draftPredictions),
      (site.getTeamPitching.getAllRatings.asScala.toSeq, predictor))

    w.format("%nActive Players%n")
    print(dHitting, dPitching)(w)

    w.format("%nDrafted Players%n")
    print(draftHitting, draftPitching)(w)
  }
}

class PlayerDevelopment(from: (Player, Predictor), to: (Player, Predictor))
    extends Scoreable {

  def this(pid: PlayerId,
           from: (TeamStats[_], Predictor),
           to: (TeamStats[_], Predictor)) =
    this((from._1.getPlayer(pid), from._2), (to._1.getPlayer(pid), to._2))

  val fromP = from._1
  val toP = to._1

  val fromV = NowAbility(fromP)(from._2)
  val toV = NowAbility(toP)(to._2)

  val score = toV.score - fromV.score

  def format[T: Regressable](regressedOn: Player => Splits[T]) = {
    def ratingChange(f: Option[Double], t: Option[Double]): Option[Double] =
      for { fr <- f; tr <- t } yield tr - fr

    def formatRatingsChanges(from: T, to: T): String = {
      val r = implicitly[Regressable[T]]

      (r.toInput(from).toOptionList, r.toInput(to).toOptionList).zipped.map {
        case (f, t) => ratingChange(f, t)
      }.map(_.map(v => if (v.round == 0) "   " else f"${v.round}%+3d").getOrElse("   ")).mkString("")
    }

    val info = f"${toP.getListedPosition.or("")}%2s ${StringUtils
      .abbreviate(toP.getName(), 25)}%-25s ${toP.getAge}%2s"
    val fromAndTo = f"${fromV.format}%s | ${toV.format}"

    val vsL = formatRatingsChanges(regressedOn(fromP).getVsLeft,
                                   regressedOn(toP).getVsLeft)
    val vsR = formatRatingsChanges(regressedOn(fromP).getVsRight,
                                   regressedOn(toP).getVsRight)

    val stars = toP.getStars.get.getFormattedText

    f"$info | $fromAndTo | ${score.toLong}%+3d | ${vsL} : ${vsR} | ${stars}"
  }
}

object PlayerDevelopment {

  private def playerIdSet(s: Seq[Player]) = s.toSet.map((p: Player) => p.getId)

  def betweenTeamStats(from: (TeamStats[_], Predictor),
                       to: (TeamStats[_], Predictor)): Seq[PlayerDevelopment] =
    betweenPlayers((from._1.getAllRatings.asScala.toSeq, from._2),
                   (to._1.getAllRatings.asScala.toSeq, to._2))

  def betweenPlayers(from: (Seq[Player], Predictor),
                     to: (Seq[Player], Predictor)): Seq[PlayerDevelopment] = {
    val pids = playerIdSet(from._1).intersect(playerIdSet(to._1))

    pids
      .map(
        pid =>
          new PlayerDevelopment((from._1.find(_.getId == pid).get, from._2),
                                (to._1.find(_.getId == pid).get, to._2)))
      .toList
  }

}