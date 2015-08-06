package com.github.lstephen.ootp.ai.selection.depthchart

import com.github.lstephen.ootp.ai.player.Player
import com.github.lstephen.ootp.ai.player.ratings.Position
import com.github.lstephen.ootp.ai.regression.Predictions
import com.github.lstephen.ootp.ai.selection.bench.BenchScorer
import com.github.lstephen.ootp.ai.selection.lineup.All
import com.github.lstephen.ootp.ai.selection.lineup.AllLineups
import com.github.lstephen.ootp.ai.selection.lineup.Defense
import com.github.lstephen.ootp.ai.selection.lineup.InLineupScore
import com.github.lstephen.ootp.ai.selection.lineup.Lineup
import com.github.lstephen.ootp.ai.selection.lineup.Lineup.VsHand

import collection.JavaConversions._

class DepthChartSelection(implicit predictions: Predictions) {

  def select(lineups: AllLineups, available: java.lang.Iterable[Player]): AllDepthCharts =
    select(lineups, Set() ++ available)

  def select(lineups: AllLineups, available: Set[Player]): AllDepthCharts = {
    AllDepthCharts.create(All
      .builder()
      .vsRhp(select(lineups.getVsRhp(), available, VsHand.VS_RHP))
      .vsRhpPlusDh(select(lineups.getVsRhpPlusDh(), available, VsHand.VS_RHP))
      .vsLhp(select(lineups.getVsLhp(), available, VsHand.VS_LHP))
      .vsLhpPlusDh(select(lineups.getVsLhpPlusDh(), available, VsHand.VS_LHP))
      .build());
  }

  def select(lineup: Lineup, available: Set[Player], vs: VsHand): DepthChart = {
    val dc = new DepthChart

    val bench = available -- lineup.playerSet

    for (entry <- lineup if entry.getPositionEnum != Position.PITCHER) {
      dc.setStarter(entry.getPositionEnum, entry.getPlayer)

      if (entry.getPositionEnum != Position.DESIGNATED_HITTER) {
        selectDefensiveReplacement(entry.getPositionEnum, entry.getPlayer, bench)
          .foreach(dc.setDefensiveReplacement(entry.getPositionEnum, _))
      }

      addBackups(dc, entry.getPositionEnum(), bench, vs)
    }

    dc
  }

  def selectDefensiveReplacement(position: Position, starter: Player, bench: Set[Player]): Option[Player] = {
    if (bench.isEmpty) return None

    val candidate = bench
      .toSeq
      .sortBy(_.getDefensiveRatings.getPositionScore(position))
      .reverse
      .head

    def defense(p: Player): Double = p.getDefensiveRatings.getPositionScore(position)

    if (defense(candidate) > defense(starter)) Some(candidate) else None
  }

  def addBackups(dc: DepthChart, position: Position, bench: Set[Player], vs: VsHand): Unit = {
    val starter = dc getStarter position
    val backups = InLineupScore.sort(bench, position, vs).take(3)

    DepthChartSelection.AllPcts
      .map((starter +: backups) zip _)
      .maxBy(score(_, position, vs))
      .filter { case (ply, _) => ply != starter }
      .filter { case (_, pct) => pct > 0 }
      .foreach { case (ply, pct) => dc.addBackup(position, ply, pct) }
  }

  def score(players: Traversable[(Player, Int)], position: Position, vs: VsHand): Double = {
    players.map { case (ply, pct) => score(ply, pct, position, vs) }.sum
  }

  def score(player: Player, pct: Int, position: Position, vs: VsHand): Double = {
    val base = InLineupScore(player, position, vs).total * pct
    val fatigue = (pct * Defense.getPositionFactor(position) * pct / 10) / 2

    // simplifies to (s/100)p - (f/2000)p^2
    (base - fatigue) / 100.0
  }
}

object DepthChartSelection {

  val AllPcts = {
    val bu1 = 1 +: (5 until 95 by 5)
    val bu2 = 0 until 50 by 5
    val bu3 = 0 until 50 by 5

    bu1
      .flatMap(b => bu2.map(List(b, _)))
      .flatMap(p => bu3.map(p :+ _))
      .filter(_.sum < 100)
      .filter(p => p(1) <= p(0))
      .filter(p => p(2) <= p(1))
      .map(p => (100 - p.sum) +: p)
  }
}

