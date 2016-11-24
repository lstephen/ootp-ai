package com.github.lstephen.ootp.ai.regression

import com.github.lstephen.ootp.ai.io.Printable
import com.github.lstephen.ootp.ai.player.Player
import com.github.lstephen.ootp.ai.player.ratings.BattingRatings
import com.github.lstephen.ootp.ai.player.ratings.PitchingRatings
import com.github.lstephen.ootp.ai.site.Site
import com.github.lstephen.ootp.ai.splits.Splits
import com.github.lstephen.ootp.ai.stats.BattingStats
import com.github.lstephen.ootp.ai.stats.History
import com.github.lstephen.ootp.ai.stats.PitchingStats
import com.github.lstephen.ootp.ai.stats.SplitPercentagesHolder
import com.github.lstephen.ootp.ai.stats.SplitStats
import com.github.lstephen.ootp.ai.stats.Stats
import com.github.lstephen.ootp.ai.stats.TeamStats

import java.io.PrintWriter

import org.apache.commons.math3.stat.regression.SimpleRegression

import scala.collection.JavaConverters._

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import scala.math._

import Regressable._

/*trait RegressionDefinition {
  type Stats
  type Ratings

  abstract def getTeamStats(s: Site): TeamStats[Stats]
}*/

case class RegressOn[S](val name: String,
                        val getStat: S => Double,
                        val setStat: (S, Long) => Unit)

class BattingRegression(site: Site) extends SiteRegression(site) {
  type R = BattingRatings[_]
  type S = BattingStats

  val regressable = implicitly[Regressable[R]]

  lazy val stats = site.getTeamBatting

  val leagueBatting = site.getLeagueBatting

  def newStats = {
    val bs = new BattingStats
    bs.setLeagueBatting(leagueBatting)
    bs
  }

  lazy val regressOn: Seq[RegressOn[BattingStats]] = Seq(
    RegressOn("Hits", _.getHitsPerPlateAppearance, _.setHits(_)),
    RegressOn("Doubles", _.getDoublesPerPlateAppearance, _.setDoubles(_)),
    RegressOn("Triples", _.getTriplesPerPlateAppearance, _.setTriples(_)),
    RegressOn("HomeRuns", _.getHomeRunsPerPlateAppearance, _.setHomeRuns(_)),
    RegressOn("Walks", _.getWalksPerPlateAppearance, _.setWalks(_)),
    RegressOn("Ks", _.getKsPerPlateAppearance, _.setKs(_)))

  def getRatings(p: Player) = p.getBattingRatings
  def getPotentialRatings(p: Player) = p.getBattingPotentialRatings

  def saveHistory(h: History, s: TeamStats[BattingStats]) =
    h.saveBatting(s, site, currentSeason)
  def loadHistory(h: History) =
    h.loadBatting(site, currentSeason, 5).asScala.toSeq
}

class PitchingRegression(site: Site) extends SiteRegression(site) {
  type R = PitchingRatings[_]
  type S = PitchingStats

  val regressable = implicitly[Regressable[R]]

  lazy val stats = site.getTeamPitching

  val leaguePitching = site.getLeaguePitching

  def newStats = {
    val ps = new PitchingStats
    ps.setLeaguePitching(leaguePitching)
    ps
  }

  lazy val regressOn: Seq[RegressOn[PitchingStats]] = Seq(
    RegressOn("Hits", _.getHitsPerPlateAppearance, _.setHits(_)),
    RegressOn("Doubles", _.getDoublesPerPlateAppearance, _.setDoubles(_)),
    RegressOn("Triples", _.getTriplesPerPlateAppearance, _.setTriples(_)),
    RegressOn("HomeRuns", _.getHomeRunsPerPlateAppearance, _.setHomeRuns(_)),
    RegressOn("Walks", _.getWalksPerPlateAppearance, _.setWalks(_)),
    RegressOn("Ks", _.getStrikeoutsPerPlateAppearance, _.setStrikeouts(_)))

  def getRatings(p: Player) = p.getPitchingRatings
  def getPotentialRatings(p: Player) = p.getPitchingPotentialRatings

  def saveHistory(h: History, s: TeamStats[PitchingStats]) =
    h.savePitching(s, site, currentSeason)
  def loadHistory(h: History) =
    h.loadPitching(site, currentSeason, 5).asScala.toSeq
}

abstract class SiteRegression(site: Site) {
  type R
  type S <: Stats[S]

  implicit val regressable: Regressable[R]

  val stats: TeamStats[S]

  def newStats: S

  val regressOn: Seq[RegressOn[S]]

  def getRatings(p: Player): Splits[R]
  def getPotentialRatings(p: Player): Splits[_ <: R]

  def saveHistory(h: History, s: TeamStats[S]): Unit
  def loadHistory(h: History): Seq[TeamStats[S]]

  val currentSeason = site.getDate.getYear

  val regressions: Future[Map[String, Regression]] = Future {
    val rs = regressOn.map(r => r.name -> new Regression(r.name)).toMap

    def getRegression(r: RegressOn[_]) =
      rs.get(r.name).getOrElse(throw new IllegalArgumentException(r.name))

    def addEntry(stats: S, ratings: R): Unit =
      (1 to stats.getPlateAppearances).foreach { _ =>
        regressOn.foreach(r =>
          getRegression(r).addData(ratings, r.getStat(stats)))
      }

    def addData(teamStats: TeamStats[S]): Unit =
      teamStats.getPlayers().asScala.foreach { p =>
        val stats = teamStats.getSplits(p)
        val ratings = getRatings(p)

        addEntry(stats.getVsLeft(), ratings.getVsLeft())
        addEntry(stats.getVsRight(), ratings.getVsRight())
      }

    addData(stats)

    val history = History.create

    saveHistory(history, stats)

    loadHistory(history).foreach(addData(_))

    rs.values.par.foreach(_.train)

    rs
  }

  val defaultPlateAppearances = 700

  def getRegression(r: RegressOn[_]) =
    Await
      .result(regressions, 10.minutes)
      .get(r.name)
      .getOrElse(throw new IllegalArgumentException(r.name))

  def predict(ps: Seq[Player], f: (Player => Splits[_ <: R]) = getRatings(_))
    : Map[Player, SplitStats[S]] =
    ps.par.map(p => p -> predict(f(p))).seq.toMap

  def predictFuture(ps: Seq[Player]): Map[Player, SplitStats[S]] =
    predict(ps, getPotentialRatings(_))

  def predict(ratings: Splits[_ <: R]): SplitStats[S] = {
    val vsRightPa = round(
      defaultPlateAppearances * SplitPercentagesHolder.get.getVsRhpPercentage)
    val vsLeftPa = defaultPlateAppearances - vsRightPa

    SplitStats.create(predict(ratings.getVsLeft, vsLeftPa * 100),
                      predict(ratings.getVsRight, vsRightPa * 100))
  }

  def predict(ratings: R, pas: Long): S = {
    def p(ro: RegressOn[_]) = round(pas * predict(ro, ratings))

    val s = newStats

    regressOn.foreach(ro => ro.setStat(s, p(ro)))

    s.setPlateAppearances(pas.toInt)

    s
  }

  def predict(ro: RegressOn[_], r: R): Double =
    max(0, getRegression(ro).predict(r))

  def correlationReport: Printable = new Printable {
    def print(w: PrintWriter): Unit = {
      w.println

      regressOn.foreach(r => w.println(getRegression(r).format))
    }
  }
}
