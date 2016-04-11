package com.github.lstephen.ootp.ai.report;

import com.google.common.base.Function;
import com.google.common.collect.Ordering;
import com.github.lstephen.ootp.ai.io.Printable;
import com.github.lstephen.ootp.ai.player.Player;
import com.github.lstephen.ootp.ai.roster.Team;
import com.github.lstephen.ootp.ai.site.Salary;
import com.github.lstephen.ootp.ai.site.Site;
import com.github.lstephen.ootp.ai.value.TradeValue;

import java.io.PrintWriter;
import java.text.NumberFormat;

import org.apache.commons.lang3.StringUtils;

import collection.JavaConversions._

/**
 *
 * @author lstephen
 */
class SalaryReport(team: Team, salary: Salary, tv: TradeValue) extends Printable {

    val currentTotal: Int = team.map(salary.getCurrentSalary(_).toInt).sum
    val nextTotal: Int = team.map(salary.getNextSalary(_).toInt).sum

    val replCurrentTotal: Int = team
      .filter(tv.getCurrentValueVsReplacement(_) > 0)
      .map(tv.getCurrentValueVsReplacement(_).toInt)
      .sum

    val replNextTotal: Int = team
      .filter(tv.getCurrentValueVsReplacement(_) > 0)
      .filter(salary.getNextSalary(_) > 0)
      .map(tv.getCurrentValueVsReplacement(_).toInt)
      .sum

    def format(i: Int): String = NumberFormat.getIntegerInstance().format(i)

    def print(w: PrintWriter): Unit = {
        w.println()

        team
          .toSeq
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

            w println f"$position%2s $name%15s $age%2d| $current%11s $next%11s"
        }

        val line = "-" * 45

        val totalCurrent = format(currentTotal)
        val totalNext = format(nextTotal)
        val buffer = " " * 21

        val perReplLabel = "$/Repl"
        val perReplCurrent = format(currentTotal / replCurrentTotal)
        val perReplNext = format(nextTotal / replNextTotal)


        w println line
        w println f"$buffer| $totalCurrent%11s $totalNext%11s"
        w println f"$perReplLabel%21s| $perReplCurrent%11s $perReplNext%11s"
        w println line
    }

}
