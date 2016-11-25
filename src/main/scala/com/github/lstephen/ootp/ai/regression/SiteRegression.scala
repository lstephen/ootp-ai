package com.github.lstephen.ootp.ai.regression

import com.github.lstephen.ootp.ai.io.{Printable, Printables}
import com.github.lstephen.ootp.ai.player.Player
import com.github.lstephen.ootp.ai.player.ratings.{
  BattingRatings,
  PitchingRatings
}
import com.github.lstephen.ootp.ai.site.Site
import com.github.lstephen.ootp.ai.splits.Splits
import com.github.lstephen.ootp.ai.stats._

import java.io.PrintWriter

import org.apache.commons.math3.stat.regression.SimpleRegression

import scala.collection.JavaConverters._

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

import scala.math._

import com.typesafe.scalalogging.LazyLogging

import Regressable._

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

abstract class SiteRegression(site: Site) extends LazyLogging {
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

  lazy val regressions: Map[String, Regression] = {
    logger.info("Running regressions...")
    val rs = regressOn.map(r => r.name -> new Regression(r.name)).toMap

    def getRegression(r: RegressOn[_]) =
      rs.get(r.name).getOrElse(throw new IllegalArgumentException(r.name))

    def addEntry(stats: S, ratings: R): Unit =
      if (stats.getPlateAppearances > 100) {
        regressOn.foreach(r =>
          getRegression(r).addData(ratings, r.getStat(stats)))
      }

    def addData(teamStats: TeamStats[S]): Unit =
      teamStats.getPlayers().asScala.foreach { p =>
        val stats = teamStats.getSplits(p)
        val ratings = getRatings(p)

        if (ratings == null) {
          logger.warn(
            s"No ratings for ${p.getShortName} (${p.getId}) aged ${p.getAge}")
        } else {
          addEntry(stats.getVsLeft(), ratings.getVsLeft())
          addEntry(stats.getVsRight(), ratings.getVsRight())
        }
      }

    addData(stats)

    val history = History.create

    saveHistory(history, stats)

    loadHistory(history).foreach(addData(_))

    logger.info("Training...")
    rs.values.foreach(_.train)

    logger.info("Regressions done.")
    rs
  }

  val defaultPlateAppearances = 700

  def getRegression(r: RegressOn[_]) =
    regressions
      .get(r.name)
      .getOrElse(throw new IllegalArgumentException(r.name))

  def predict(ps: Seq[Player], f: (Player => Splits[_ <: R]) = getRatings(_))
    : Map[Player, SplitStats[S]] =
    (ps, predict(ps map f)).zipped.toMap

  def predictFuture(ps: Seq[Player]): Map[Player, SplitStats[S]] =
    predict(ps, getPotentialRatings(_))

  def predict(ratings: Seq[Splits[_ <: R]]): Seq[SplitStats[S]] = {
    val vsRightPa = round(
      defaultPlateAppearances * SplitPercentagesHolder.get.getVsRhpPercentage)
    val vsLeftPa = defaultPlateAppearances - vsRightPa

    val vsLeft = predict(ratings.map(_.getVsLeft), vsLeftPa * 100)
    val vsRight = predict(ratings.map(_.getVsRight), vsRightPa * 100)

    (vsLeft, vsRight).zipped.map(SplitStats.create(_, _))
  }

  def predict(ratings: Seq[R], pas: Long): Seq[S] = {
    val stats = Seq.fill(ratings.size) { newStats }

    regressOn.foreach(ro =>
      (predict(ro, ratings), stats).zipped.foreach {
        case (d, s) => ro.setStat(s, round(pas * d))
    })

    stats.foreach(_.setPlateAppearances(pas.toInt))

    stats
  }

  def predict(ro: RegressOn[_], r: Seq[R]): Seq[Double] =
    getRegression(ro).predict(r).map(max(0, _))

  def correlationReport: Printable = new Printable {
    def print(w: PrintWriter): Unit = {
      w.println

      regressOn.foreach(r => w.println(getRegression(r).format))

      w.println(s"Features: ${regressable.features.mkString(", ")}")
      regressOn.foreach(r =>
        Printables.print(getRegression(r).modelReport).to(w))
    }
  }
}