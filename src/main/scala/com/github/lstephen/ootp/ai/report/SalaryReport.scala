package com.github.lstephen.ootp.ai.report;

import com.google.common.base.Function
import com.google.common.collect.Ordering
import com.github.lstephen.ootp.ai.io.Printable
import com.github.lstephen.ootp.ai.player.Player
import com.github.lstephen.ootp.ai.regression.Predictor
import com.github.lstephen.ootp.ai.roster.Team
import com.github.lstephen.ootp.ai.score.Score
import com.github.lstephen.ootp.ai.site.Financials
import com.github.lstephen.ootp.ai.site.Salary
import com.github.lstephen.ootp.ai.site.Site
import com.github.lstephen.ootp.ai.value.NowValue
import com.github.lstephen.ootp.ai.value.SalaryPredictor

import java.io.PrintWriter;
import java.text.NumberFormat;

import org.apache.commons.lang3.StringUtils;

import collection.JavaConversions._

import scala.math._

/**
  *
  * @author lstephen
  */
class SalaryReport(team: Team, salary: Salary, financials: Financials)(
    implicit predictor: Predictor)
    extends SalaryPredictor
    with Printable {

  private def value(p: Player): Score = {
    val v = NowValue(p)

    v.vsReplacement.orElseZero + v.vsMax.orElseZero
  }

  val currentTotal: Long = team.map(salary.getCurrentSalary(_).toLong).sum
  val nextTotal: Long = team.map(salary.getNextSalary(_).toLong).sum

  val replCurrentTotal: Score =
    team.map(value(_)).filter(_.isPositive).total

  val replNextTotal: Score = team
    .filter(salary.getNextSalary(_) > 0)
    .map(value(_))
    .filter(_.isPositive)
    .total

  val maxCurrent = currentTotal + financials.getAvailableForFreeAgents
  val maxNext = nextTotal + financials.getAvailableForExtensions
  val maxReplCurrent = maxCurrent / replCurrentTotal.toLong
  val maxReplNext = maxNext / replNextTotal.toLong

  def format(i: Long): String = NumberFormat.getIntegerInstance().format(i)
  def format(i: Int): String = NumberFormat.getIntegerInstance().format(i)

  def print(w: PrintWriter): Unit = {
    w.println()

    team.toSeq
      .filter(salary.getCurrentSalary(_) != 0)
      .sortBy(salary.getCurrentSalary(_))
      .reverse
      .foreach { p =>
        val s = salary getCurrentSalary p

        val nextS = salary getNextSalary p

        val position = p.getPosition
        val name = StringUtils.abbreviate(p.getShortName, 15)
        val age = p.getAge
        val current = format(s)
        val next = if (nextS == 0) "" else format(s)

        w println f"$position%2s $name%-15s $age%2d| $current%11s $next%11s"
      }

    val line = "-" * 45

    val totalCurrent = format(currentTotal)
    val totalNext = format(nextTotal)
    val buffer = " " * 21

    val perReplLabel = "$/Value"
    val perReplCurrent = format(currentTotal / replCurrentTotal.toLong)
    val perReplNext = format(nextTotal / replNextTotal.toLong)

    val forLabel = "$ Available"
    val forFreeAgents = format(financials.getAvailableForFreeAgents)
    val forExtensions = format(financials.getAvailableForExtensions)

    val maxLabel = "Max $/Value"

    w println line
    w println f"$buffer| $totalCurrent%11s $totalNext%11s"
    w println f"$perReplLabel%21s| $perReplCurrent%11s $perReplNext%11s"
    w println f"$forLabel%21s| $forFreeAgents%11s $forExtensions%11s"
    w println f"$maxLabel%21s| ${format(maxReplCurrent)}%11s ${format(maxReplNext)}%11s"
    w println line
  }

  def predictNow(p: Player): Integer =
    max(value(p).toLong * maxReplCurrent, 0).toInt

  def predictNext(p: Player): Integer =
    max(value(p).toLong * maxReplNext, 0).toInt

}
