package com.github.lstephen.ootp.ai.selection.depthchart

import com.github.lstephen.ootp.ai.player.Player
import com.github.lstephen.ootp.ai.player.ratings.Position
import com.github.lstephen.ootp.ai.regression.Predictions
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
    val backups = InLineupScore.sort(bench, position, vs).take(2)

    var primary = dc.getStarter(position)
    var remaining = 100.0

    for (backup <- backups) {
      val pct = backupPercentage(position, primary, backup, vs)

      val primaryPct = remaining - (pct * remaining)

      if (backups.contains(primary) && primaryPct >= 1) {
        dc.addBackup(position, primary, roundPercentage(primaryPct))
      }

      remaining -= primaryPct
      primary = backup
    }

    if (roundPercentage(remaining) > 1) {
      dc.addBackup(position, primary, roundPercentage(remaining))
    }
  }

  def backupPercentage(p: Position, primary: Player, backup: Player, vs: VsHand): Double = {
    def ability(ply: Player): Double = InLineupScore(ply, p, vs).total

    // The max is needed because sometimes the lineup chosen doesn't have the best player.
    // There is probably a tweak or a bug in the lineup selection or defense selection.
    val daysOff = (ability(primary) - ability(backup)).max(0) / Defense.getPositionFactor(p) + 1

    1 / (daysOff + 1)
  }

  def roundPercentage(pct: Double): Long = {
    val rounded = (pct/5.0).round * 5

    if (rounded == 0) 1 else rounded
  }
}


