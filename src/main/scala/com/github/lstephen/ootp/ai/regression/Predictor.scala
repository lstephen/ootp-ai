package com.github.lstephen.ootp.ai.regression

import com.github.lstephen.ootp.ai.io.Printable
import com.github.lstephen.ootp.ai.player.Player
import com.github.lstephen.ootp.ai.score._
import com.github.lstephen.ootp.ai.site.Site
import com.github.lstephen.ootp.ai.stats.BattingStats
import com.github.lstephen.ootp.ai.stats.PitchingStats
import com.github.lstephen.ootp.ai.stats.SplitStats

import com.typesafe.scalalogging.LazyLogging

import java.io.PrintWriter

import humanize.Humanize

import scala.collection.JavaConverters._

class Predictor(ps: Seq[Player],
                private val br: BattingRegression,
                private val pr: PitchingRegression)
    extends LazyLogging {

  private def time[R](label: String, block: => R): R = {
    val t0 = System.nanoTime

    try {
      block
    } finally {
      val t1 = System.nanoTime
      logger info s"$label predict execution time: ${Humanize.nanoTime(t1 - t0)}"
    }
  }

  val batting =
    time("batting", br.predict(ps).mapValues(new BattingPrediction(_)))

  val battingFuture = time(
    "battingFuture",
    br.predictFuture(ps).mapValues(new BattingPrediction(_)))

  val pitching = time(
    "pitching",
    pr.predict(ps.filter(_.isPitcher)).mapValues(new PitchingPrediction(_)))

  val pitchingFuture = time(
    "pitchingFuture",
    pr.predict(ps.filter(_.isPitcher)).mapValues(new PitchingPrediction(_)))

  private def getPrediction[P](ps: Map[Player, P], p: Player): P =
    ps.get(p)
      .getOrElse(
        throw new IllegalArgumentException(
          s"Unable to predict for: ${p.getId}, ${p.getShortName}"))

  def predictBatting(p: Player): BattingPrediction = getPrediction(batting, p)
  def predictFutureBatting(p: Player): BattingPrediction =
    getPrediction(battingFuture, p)

  def predictPitching(p: Player): PitchingPrediction =
    getPrediction(pitching, p)
  def predictFuturePitching(p: Player): PitchingPrediction =
    getPrediction(pitchingFuture, p)

  def correlationReport: Printable = new Printable {
    def print(w: PrintWriter) = {
      br.correlationReport.print(w)
      pr.correlationReport.print(w)
    }
  }

  def this(ps: Seq[Player], pr: Predictor) = this(ps, pr.br, pr.pr)

  // Java compatability
  def this(ps: java.lang.Iterable[Player], pr: Predictor) =
    this(ps.asScala.toSeq, pr.br, pr.pr)

}

object Predictor {
  def train(site: Site): Predictor =
    new Predictor(site.getAllPlayers().asScala.toSeq,
                  new BattingRegression(site),
                  new PitchingRegression(site))

}

class BattingPrediction(stats: SplitStats[BattingStats]) {
  val overall = Score(stats.getOverall.getWobaPlus)
  val vsLeft = stats.getVsLeft
  val vsRight = stats.getVsRight
}

class PitchingPrediction(stats: SplitStats[PitchingStats]) {
  val overall = Score(stats.getOverall.getBaseRunsPlus)
  val vsBoth = stats.getOverall
  val vsLeft = stats.getVsLeft
  val vsRight = stats.getVsRight
}
