package com.github.lstephen.ootp.ai.regression

import com.github.lstephen.ootp.ai.io.Printable
import com.github.lstephen.ootp.ai.player.Player
import com.github.lstephen.ootp.ai.player.ratings.BattingRatings
import com.github.lstephen.ootp.ai.site.Site
import com.github.lstephen.ootp.ai.splits.Splits
import com.github.lstephen.ootp.ai.stats.BattingStats
import com.github.lstephen.ootp.ai.stats.History
import com.github.lstephen.ootp.ai.stats.SplitPercentagesHolder
import com.github.lstephen.ootp.ai.stats.SplitStats
import com.github.lstephen.ootp.ai.stats.TeamStats

import java.io.PrintWriter

import org.apache.commons.math3.stat.regression.SimpleRegression

import scala.collection.JavaConverters._

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import scala.math._

class BattingRegression(site: Site) {

  val stats = site.getTeamBatting

  object Predicting extends Enumeration {
    type Predicting = Value
    val Hits, Doubles, Triples, HomeRuns, Walks, Ks = Value
  }
  import Predicting._

  val regressions: Future[Map[Predicting, Regression]] = Future {
    val rs = Predicting.values.map(p => p -> new Regression(p.toString, "Batting")).toMap

    def getRegression(p: Predicting) = rs.get(p).getOrElse(throw new IllegalArgumentException(p.toString))

    def addEntry(stats: BattingStats, ratings: BattingRatings[_]): Unit =
      (1 to stats.getPlateAppearances).foreach { _ =>
        getRegression(Hits).addData(ratings, stats.getHitsPerPlateAppearance)
        getRegression(Doubles).addData(ratings, stats.getDoublesPerPlateAppearance)
        getRegression(Triples).addData(ratings, stats.getTriplesPerPlateAppearance)
        getRegression(HomeRuns).addData(ratings, stats.getHomeRunsPerPlateAppearance)
        getRegression(Walks).addData(ratings, stats.getWalksPerPlateAppearance)

        if (ratings.getK.isPresent) {
          getRegression(Ks).addData(ratings, stats.getKsPerPlateAppearance)
        }
      }

    def addData(teamStats: TeamStats[BattingStats]): Unit =
      teamStats.getPlayers().asScala.foreach { p =>
        val stats = teamStats.getSplits(p)
        val ratings = p.getBattingRatings()

        addEntry(stats.getVsLeft(), ratings.getVsLeft())
        addEntry(stats.getVsRight(), ratings.getVsRight())
      }

    addData(stats)

    val history = History.create

    val currentSeason = site.getDate.getYear

    history.saveBatting(stats, site, currentSeason)

    history.loadBatting(site, currentSeason, 5).asScala.toSeq.foreach(addData(_))

    rs.values.par.foreach(_.train)

    rs
  }

  val defaultPlateAppearances = 700

  val leagueBatting = site.getLeagueBatting

  def getRegression(p: Predicting) = Await.result(regressions, 10.minutes).get(p).getOrElse(throw new IllegalArgumentException(p.toString))

  def predict(p: Predicting, r: BattingRatings[_]): Double =
    max(0, getRegression(p).predict(r))

  def predict(ps: Seq[Player], f: (Player => SplitStats[BattingStats]) = predict(_)): Map[Player, SplitStats[BattingStats]] = ps.par.map(p => p -> f(p)).seq.toMap
  def predictFuture(ps: Seq[Player]): Map[Player, SplitStats[BattingStats]] = predict(ps, predictFuture(_))

  def predict(p: Player): SplitStats[BattingStats] = predict(p.getBattingRatings)
  def predictFuture(p: Player): SplitStats[BattingStats] = predict(p.getBattingPotentialRatings)

  def predict(ratings: Splits[_ <: BattingRatings[_]]): SplitStats[BattingStats] = {
    val vsRightPa = round(defaultPlateAppearances * SplitPercentagesHolder.get.getVsRhpPercentage)
    val vsLeftPa = defaultPlateAppearances - vsRightPa

    SplitStats.create(predict(ratings.getVsLeft, vsLeftPa * 100), predict(ratings.getVsRight, vsRightPa * 100))
  }

  def predict(ratings: BattingRatings[_], pas: Long): BattingStats = {
    def p(pd: Predicting) = round(pas * predict(pd, ratings))

    val bs = new BattingStats

    bs.setLeagueBatting(leagueBatting)
    bs.setHits(p(Hits))
    bs.setDoubles(p(Doubles))
    bs.setTriples(p(Triples))
    bs.setHomeRuns(p(HomeRuns))
    bs.setWalks(p(Walks))

    // Should be ok using 0 as sefault here instead of average, since any
    // player we care about their k prediction will have a k rating
    bs.setKs(if (ratings.getK.isPresent) p(Ks) else 0)

    bs.setAtBats(round(pas - bs.getWalks))

    bs
  }

  def correlationReport: Printable = new Printable {
    def print(w: PrintWriter): Unit = {
      w.println

      Predicting.values.foreach(p => w.println(getRegression(p).format))
    }
  }
}

