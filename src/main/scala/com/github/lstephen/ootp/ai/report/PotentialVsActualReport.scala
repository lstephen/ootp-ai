package com.github.lstephen.ootp.ai.report

import com.github.lstephen.ootp.ai.io.Printable
import com.github.lstephen.ootp.ai.player.Player
import com.github.lstephen.ootp.ai.player.ratings.{
  BattingRatings,
  PitchingRatings
}
import com.github.lstephen.ootp.ai.regression.Regressable
import com.github.lstephen.ootp.ai.site.Site
import com.github.lstephen.ootp.ai.splits.Splits
import com.github.lstephen.ootp.ai.stats.{History, TeamStats}

import scala.collection.mutable.HashMap
import scala.collection.mutable.{Set => MutableSet}

import scala.collection.JavaConverters._

import java.io.PrintWriter

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics

class PotentialVsActualReport(site: Site) extends Printable {

  def ignore(feature: String): Boolean =
    Array("Running Speed", "Runs", "Groundball Pct.", "Endurance").contains(
      feature)

  val history = History.create

  case class Bucket(feature: String, age: Integer, potential: Long)

  def mkBucket(f: String, a: Integer, p: Option[Double]): Option[Bucket] =
    for { pr <- p } yield Bucket(f, a, pr.round)

  def data[R: Regressable](
      loadHistory: Int => TeamStats[_],
      getPotential: Player => R,
      getActual: Player => Splits[R]): Map[Bucket, DescriptiveStatistics] = {
    val m = new HashMap[Bucket, DescriptiveStatistics]()

    def add(b: Option[Bucket], a: Option[Double]): Unit = {
      for { bk <- b; ar <- a } {
        val ds = m.get(bk).getOrElse(new DescriptiveStatistics)

        ds.addValue(ar)

        m.put(bk, ds)
      }
    }

    val r = implicitly[Regressable[R]]

    (-10 to -1)
      .map(loadHistory(_))
      .toList
      .filter(_ != null)
      .flatMap(_.getAllRatings.asScala)
      .filter(getPotential(_) != null)
      .filter(getActual(_) != null)
      .foreach { p =>
        r.features.filter(!ignore(_)).zipWithIndex.foreach {
          case (l, i) => {
            add(mkBucket(l, p.getAge, r.toInput(getPotential(p)).get(i)),
                r.toInput(getActual(p).getVsLeft).get(i))
            add(mkBucket(l, p.getAge, r.toInput(getPotential(p)).get(i)),
                r.toInput(getActual(p).getVsRight).get(i))
          }
        }
      }

    m.toMap
  }

  val hittingData: Map[Bucket, DescriptiveStatistics] =
    data(history.loadBatting(site, _),
         _.getRawBattingPotential,
         _.getBattingRatings)

  val pitchingData: Map[Bucket, DescriptiveStatistics] =
    data(history.loadPitching(site, _),
         _.getRawPitchingPotential,
         _.getPitchingRatings)

  val allPotentials: List[Long] =
    (hittingData.keys ++ pitchingData.keys)
      .map { case Bucket(f, a, p) => p }
      .toList
      .sorted

  def nextPotential(p: Long): Option[Long] =
    allPotentials.lift(allPotentials.indexOf(p) + 1)
  def prevPotential(p: Long): Option[Long] =
    allPotentials.lift(allPotentials.indexOf(p) - 1)

  val hittingMean = mean(hittingData) _
  val pitchingMean = mean(pitchingData) _

  def mean(d: Map[Bucket, DescriptiveStatistics])(f: String,
                                                  a: Integer,
                                                  p: Long): Option[Long] = {
    val ds = new DescriptiveStatistics

    List(
      Some(Bucket(f, a, p)), // center
      Some(Bucket(f, a - 1, p)),
      Some(Bucket(f, a + 1, p)), // age +- 1
      nextPotential(p).map(Bucket(f, a, _)),
      prevPotential(p).map(Bucket(f, a, _))
    ) // pot +- 1
    .flatten
      .map(d.get(_))
      .flatten
      .flatMap(_.getValues)
      .foreach(ds.addValue(_))

    if (ds.getN > 0) Some(ds.getMean.round) else None
  }

  def print(w: PrintWriter): Unit = {
    val r = implicitly[Regressable[BattingRatings[_]]]

    r.features.filter(!ignore(_)).foreach { f =>
      w.println()
      w.println(
        f"${f}%-15s | ${(15 to 45).map(a => f"${a}%3d").mkString(" ")} |")

      allPotentials.foreach { p =>
        w.println(f"${p}%-15s | ${(15 to 45)
          .map(a => hittingMean(f, a, p).map(m => f"${m}%3d").getOrElse("   "))
          .mkString(" ")} |")
      }
    }

    val p = implicitly[Regressable[PitchingRatings[_]]]

    p.features.filter(!ignore(_)).foreach { f =>
      w.println()
      w.println(
        f"${f}%-15s | ${(15 to 45).map(a => f"${a}%3d").mkString(" ")} |")

      allPotentials.foreach { p =>
        w.println(f"${p}%-15s | ${(15 to 45)
          .map(a => pitchingMean(f, a, p).map(m => f"${m}%3d").getOrElse("   "))
          .mkString(" ")} |")
      }
    }
  }

}
