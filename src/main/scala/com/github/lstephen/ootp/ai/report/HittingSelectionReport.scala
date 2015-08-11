package com.github.lstephen.ootp.ai.report

import com.github.lstephen.ootp.ai.io.Printable
import com.github.lstephen.ootp.ai.player.Player
import com.github.lstephen.ootp.ai.regression.Predictions
import com.github.lstephen.ootp.ai.roster.Roster
import com.github.lstephen.ootp.ai.selection.lineup.Defense
import com.github.lstephen.ootp.ai.selection.lineup.InLineupScore
import com.github.lstephen.ootp.ai.selection.lineup.Lineup.VsHand
import com.github.lstephen.ootp.ai.stats.BattingStats
import com.github.lstephen.ootp.ai.stats.TeamStats

import java.io.PrintWriter

import collection.JavaConversions._

class HittingSelectionReport(roster: Roster)(implicit predictions: Predictions, stats: TeamStats[BattingStats]) extends Printable {

  def print(w: PrintWriter): Unit = {
    w.println()

    (List() ++ roster.getAllPlayers)
      .filter(_.isHitter)
      .sortBy(InLineupScore(_).total)
      .reverse
      .foreach(p => w.println(format(p)))
  }

  def format(p: Player): String = new Formatter(p).toString

  class Formatter(p: Player) {

    override def toString: String =
      s"$info| $hitting | $defense | $intangibles || $overall || $status |"

    def info: String = f"${p.getPosition}%-2s ${p.getShortName}%-15s ${p.getAge}%2d"

    def hitting: String = s"${hitting(VsHand.VS_LHP)} | ${hitting(VsHand.VS_RHP)}"

    def hitting(vs: VsHand): String = {
      val ps = vs.getStats(predictions, p)
      val ss = if (stats.contains(p)) Some(vs.getStats(stats, p)) else None

      f"${ps.getSlashLine}%14s ${ps.getWobaPlus}%3d ${ss map (_.getWobaPlus) getOrElse ""}%3s"
    }

    def defense: String =
      f"${p.getDefensiveRatings.getPositionScores}%8s ${Defense.score(p).doubleValue.round}%3d"

    def intangibles: String = p.getIntangibles

    def overall: String = f"${InLineupScore(p).total.round}%3d"

    def status: String = {
      val level = if (roster.getStatus(p) == null) "" else roster.getStatus(p)

      f"${level}%-3s ${p.getRosterStatus}"
    }
  }
}

