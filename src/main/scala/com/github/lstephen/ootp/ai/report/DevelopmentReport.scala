package com.github.lstephen.ootp.ai.report

import com.github.lstephen.ootp.ai.draft.DraftClass
import com.github.lstephen.ootp.ai.io.Printable
import com.github.lstephen.ootp.ai.player.{Player, PlayerId}
import com.github.lstephen.ootp.ai.player.ratings.{
  BattingRatings,
  PitchingRatings
}
import com.github.lstephen.ootp.ai.regression.{Predictor, Regressable}
import com.github.lstephen.ootp.ai.site.Site
import com.github.lstephen.ootp.ai.score.{Score, Scoreable}
import com.github.lstephen.ootp.ai.score.Score._
import com.github.lstephen.ootp.ai.splits.Splits
import com.github.lstephen.ootp.ai.stats.{BattingStats, History, TeamStats}
import com.github.lstephen.ootp.ai.value.NowAbility
import java.io.PrintWriter
import org.apache.commons.lang3.StringUtils
import com.typesafe.scalalogging.StrictLogging
import scala.collection.JavaConverters._

class HistorialDevelopmentReport(site: Site, implicit val predictor: Predictor)
    extends Printable {

  val history = History.create

  def loadHistory(f: Int => TeamStats[_]): Seq[PlayerDevelopment] =
    (-10 to -1)
      .map(f(_))
      .toList
      .filter(_ != null)
      .map(h => (h, new Predictor(h.getAllRatings, predictor)))
      .sliding(2)
      .flatMap {
        case f :: t :: Nil => PlayerDevelopment.betweenTeamStats(f, t)
      }
      .toSeq

  val battingDevelopment: Seq[PlayerDevelopment] = loadHistory(
    history.loadBatting(site, _))

  val pitchingDevelopment: Seq[PlayerDevelopment] = loadHistory(
    history.loadPitching(site, _))

  def printOvrGrid(pds: Seq[PlayerDevelopment])(implicit w: PrintWriter) = {
    val cells = pds.map(pd => (pd.toP, pd.score))

    val players: Set[Player] = pds.map(_.toP).toSet

    def cellsFor(p: Player) = cells.filter(_._1 == p).sortBy(_._1.getAge)
    def cellFor(p: Player, age: Int): Option[(Player, Score)] =
      cellsFor(p).find(_._1.getAge == age)

    players.toList.sortBy(p => cellsFor(p).last._1.getAge).foreach { ply =>
      val cells = cellsFor(ply)
      val p = cells.last._1

      val formattedCells = (15 to 45)
        .map(
          a =>
            cellFor(p, a)
              .map((d: (Player, Score)) => f"${d._2.toLong}%+3d")
              .getOrElse("   "))
        .mkString(" ")

      w.println(
        f"${StringUtils.abbreviate(p.getName(), 25)}%-25s | $formattedCells |")
    }
  }

  def printBattingFeatureGrid(pds: Seq[PlayerDevelopment])(
      implicit w: PrintWriter): Unit = {
    val r = implicitly[Regressable[BattingRatings[_]]]

    r.features.zipWithIndex.foreach {
      case (l, i) => printBattingFeatureGrid(pds, l, i)
    }
  }

  def printPitchingFeatureGrid(pds: Seq[PlayerDevelopment])(
      implicit w: PrintWriter): Unit = {
    val r = implicitly[Regressable[PitchingRatings[_]]]

    r.features.zipWithIndex.foreach {
      case (l, i) => printPitchingFeatureGrid(pds, l, i)
    }
  }

  def printBattingFeatureGrid(pds: Seq[PlayerDevelopment],
                              label: String,
                              idx: Int)(implicit w: PrintWriter) =
    printFeatureGrid(pds, label, idx, _.getBattingRatings)

  def printPitchingFeatureGrid(pds: Seq[PlayerDevelopment],
                               label: String,
                               idx: Int)(implicit w: PrintWriter) =
    printFeatureGrid(pds, label, idx, _.getPitchingRatings)

  def printFeatureGrid[R: Regressable](
      pds: Seq[PlayerDevelopment],
      label: String,
      idx: Int,
      r: Player => Splits[R])(implicit w: PrintWriter) = {
    type Cell = (Player, Option[Score], Option[Score])

    val cells = pds.map(
      pd =>
        (pd.toP,
         pd.featureScore(r(_).getVsLeft, idx),
         pd.featureScore(r(_).getVsRight, idx)))

    val players: Set[Player] = pds.map(_.toP).toSet

    def cellsFor(p: Player) = cells.filter(_._1 == p).sortBy(_._1.getAge)
    def cellsForAge(age: Int) = cells.filter(_._1.getAge == age)
    def cellFor(p: Player, age: Int): Option[Cell] =
      cellsFor(p).find(_._1.getAge == age)

    def averageForAge(age: Int): Score =
      cellsForAge(age).flatMap(c => List(c._2, c._3)).flatten.average

    def formatCellsByAge(p: Player, f: Cell => Option[Score]) =
      (15 to 45)
        .map(
          a =>
            cellFor(p, a)
              .flatMap(d => f(d).map(s => f"${s.toLong}%+3d"))
              .getOrElse("   "))
        .mkString(" ")

    w.println
    w.println(
      f"${"--- " + label + " ---"}%-25s | ${(15 to 45).map(a => f"${a}%3d").mkString(" ")} |")

    w.println(
      f"${"Average"}%25s | ${(15 to 45).map(averageForAge(_)).map(s => f"${s.toLong}%+3d").mkString(" ")} |")
    w.println(
      f"${"Players"}%25s | ${(15 to 45).map(cellsForAge(_).size).map(n => f"${n}%3d").mkString(" ")} |")

    players.toList.sortBy(p => cellsFor(p).last._1.getAge).foreach { ply =>
      val cells = cellsFor(ply)
      val p = cells.last._1

      val formattedCellsVsL = formatCellsByAge(p, _._2)
      val formattedCellsVsR = formatCellsByAge(p, _._3)

      w.println(
        f"${StringUtils.abbreviate(p.getName(), 25)}%-25s | $formattedCellsVsL |")
      w.println(f"${""}%-25s | $formattedCellsVsR |")
    }
  }

  def print(w: PrintWriter): Unit = {
    w.println()
    w.println(
      f"${"Hitters"}%-25s | ${(15 to 45).map(a => f"${a}%3d").mkString(" ")} |")
    printOvrGrid(battingDevelopment.filter(_.toP.isHitter))(w)

    w.println()
    w.println(
      f"${"Pitchers"}%-25s | ${(15 to 45).map(a => f"${a}%3d").mkString(" ")} |")
    printOvrGrid(pitchingDevelopment.filter(_.toP.isPitcher))(w)

    //printBattingFeatureGrid(battingDevelopment.filter(_.toP.isHitter))(w)
    //printPitchingFeatureGrid(pitchingDevelopment.filter(_.toP.isPitcher))(w)
  }
}

class DevelopmentReport(site: Site, implicit val predictor: Predictor)
    extends Printable
    with StrictLogging {
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

    if (fromHitting == null || fromPitching == null) {
      return
    }

    fromHitting.getAllRatings.asScala
      .foreach(_.setRatingsDefinition(site.getDefinition))
    fromPitching.getAllRatings.asScala
      .foreach(_.setRatingsDefinition(site.getDefinition))

    val fromPredictions = new Predictor(
      (fromHitting.getAllRatings.asScala ++ fromPitching.getAllRatings.asScala).toSeq.distinct,
      predictor)
    val draftPredictions = new Predictor(draft.getPlayers, predictor)

    logger.info("Setting up current year development...")
    val dHitting = PlayerDevelopment.betweenTeamStats(
      (fromHitting, fromPredictions),
      (site.getTeamBatting, predictor))
    val dPitching = PlayerDevelopment.betweenTeamStats(
      (fromPitching, fromPredictions),
      (site.getTeamPitching, predictor))

    logger.info("Setting up draft development...")
    val draftHitting = PlayerDevelopment.betweenPlayers(
      (draft.getPlayers.asScala.toSeq, draftPredictions),
      (site.getTeamBatting.getAllRatings.asScala.toSeq, predictor))
    val draftPitching = PlayerDevelopment.betweenPlayers(
      (draft.getPlayers.asScala.toSeq, draftPredictions),
      (site.getTeamPitching.getAllRatings.asScala.toSeq, predictor))

    logger.info("Active Players report...")
    w.format("%nActive Players%n")
    print(dHitting, dPitching)(w)

    logger.info("Draft Players report...")
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

  def ratingChange(f: Option[Double], t: Option[Double]): Option[Double] =
    for { fr <- f; tr <- t } yield tr - fr

  def featureScore[R: Regressable](f: Player => R,
                                   idx: Integer): Option[Score] = {
    val r = implicitly[Regressable[R]]

    ratingChange(r.toInput(f(fromP)).get(idx), r.toInput(f(toP)).get(idx))
      .map(Score(_))
  }

  def format[T: Regressable](regressedOn: Player => Splits[T]) = {

    def formatRatingsChanges(from: T, to: T): String = {
      val r = implicitly[Regressable[T]]

      (r.toInput(from).toOptionList, r.toInput(to).toOptionList).zipped
        .map {
          case (f, t) => ratingChange(f, t)
        }
        .map(_.map(v => if (v.round == 0) "   " else f"${v.round}%+3d")
          .getOrElse("   "))
        .mkString("")
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

  private def playerIdSet(s: Seq[Player]) = s.map((p: Player) => p.getId).toSet

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
