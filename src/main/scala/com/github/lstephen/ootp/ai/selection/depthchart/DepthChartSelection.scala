package com.github.lstephen.ootp.ai.selection.depthchart

import com.github.lstephen.ootp.ai.player.Player
import com.github.lstephen.ootp.ai.player.ratings.Position
import com.github.lstephen.ootp.ai.regression.Predictor
import com.github.lstephen.ootp.ai.selection.bench.BenchScorer
import com.github.lstephen.ootp.ai.selection.lineup.All
import com.github.lstephen.ootp.ai.selection.lineup.AllLineups
import com.github.lstephen.ootp.ai.selection.lineup.Defense
import com.github.lstephen.ootp.ai.selection.lineup.InLineupScore
import com.github.lstephen.ootp.ai.selection.lineup.Lineup
import com.github.lstephen.ootp.ai.selection.lineup.Lineup.VsHand

import com.github.lstephen.ai.search.HillClimbing
import com.github.lstephen.ai.search.Heuristic
import com.github.lstephen.ai.search.Validator
import com.github.lstephen.ai.search.action.Action
import com.github.lstephen.ai.search.action.ActionGenerator
import com.github.lstephen.ai.search.action.SequencedAction

import collection.JavaConversions._

class DepthChartSelection(implicit predictor: Predictor) {

  def select(lineups: AllLineups,
             available: java.lang.Iterable[Player]): AllDepthCharts =
    select(lineups, Set() ++ available)

  def select(lineups: AllLineups, available: Set[Player]): AllDepthCharts = {
    AllDepthCharts.create(
      All
        .builder()
        .vsRhp(select(lineups.getVsRhp(), available, VsHand.VS_RHP))
        .vsRhpPlusDh(
          select(lineups.getVsRhpPlusDh(), available, VsHand.VS_RHP))
        .vsLhp(select(lineups.getVsLhp(), available, VsHand.VS_LHP))
        .vsLhpPlusDh(
          select(lineups.getVsLhpPlusDh(), available, VsHand.VS_LHP))
        .build());
  }

  def select(lineup: Lineup, available: Set[Player], vs: VsHand): DepthChart = {
    val dc = new DepthChart

    val bench = available -- lineup.playerSet

    for (entry <- lineup if entry.getPositionEnum != Position.PITCHER) {
      dc.setStarter(entry.getPositionEnum, entry.getPlayer)

      if (entry.getPositionEnum != Position.DESIGNATED_HITTER) {
        selectDefensiveReplacement(
          entry.getPositionEnum,
          entry.getPlayer,
          bench).foreach(dc.setDefensiveReplacement(entry.getPositionEnum, _))
      }

      addBackups(dc, entry.getPositionEnum(), bench, vs)
    }

    val leftOver = available -- lineup.playerSet -- dc.getPlayerSet

    for (ply <- leftOver if ply.isHitter) {
      val bestPos = lineup
        .map(_.getPositionEnum)
        .filter(_ != Position.PITCHER)
        .toSeq
        .filter(dc.getBackups(_).size < 3)
        .sortBy(
          pos =>
            InLineupScore(dc.getBackups(pos).head.getPlayer, pos, vs).score - InLineupScore(
              ply,
              pos,
              vs).score)
        .head

      dc.addBackup(bestPos, ply, 1)
    }

    dc
  }

  def selectDefensiveReplacement(position: Position,
                                 starter: Player,
                                 bench: Set[Player]): Option[Player] = {
    if (bench.isEmpty) return None

    val candidate = bench.toSeq
      .sortBy(_.getDefensiveRatings.getPositionScore(position))
      .reverse
      .head

    def defense(p: Player): Double =
      p.getDefensiveRatings.getPositionScore(position)

    if (defense(candidate) > defense(starter) * 1.1) Some(candidate) else None
  }

  def addBackups(dc: DepthChart,
                 position: Position,
                 bench: Set[Player],
                 vs: VsHand): Unit = {
    val starter = dc getStarter position
    val backup = InLineupScore.sort(bench, position, vs).head

    dc.addBackup(position, backup, 1)
  }
}
