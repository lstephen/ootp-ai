package com.github.lstephen.ootp.ai.selection.bench

import com.github.lstephen.ootp.ai.player.Player
import com.github.lstephen.ootp.ai.player.ratings.Position
import com.github.lstephen.ootp.ai.regression.Predictions
import com.github.lstephen.ootp.ai.selection.depthchart.DepthChart.Backup
import com.github.lstephen.ootp.ai.selection.depthchart.DepthChartSelection
import com.github.lstephen.ootp.ai.selection.lineup.Defense
import com.github.lstephen.ootp.ai.selection.lineup.InLineupScore
import com.github.lstephen.ootp.ai.selection.lineup.Lineup
import com.github.lstephen.ootp.ai.selection.lineup.Lineup.VsHand

import collection.JavaConversions._

class BenchScorer(implicit predictions: Predictions) {

  def score(p: Player, bench: java.lang.Iterable[Player], lineup: Lineup, vs: VsHand): Double = {
    score(bench, lineup, vs, p == _)
  }

  def score(bench: java.lang.Iterable[Player], lineup: Lineup, vs: VsHand): Double = {
    score(bench, lineup, vs, p => true)
  }

  def score(bench: Traversable[Player], lineup: Lineup, vs: VsHand, predicate: Player => Boolean): Double = {
    val dc = new DepthChartSelection().select(lineup, bench.toSet, vs)

    lineup
      .filter(_.getPositionEnum != Position.PITCHER)
      .flatMap { e => dc.getBackups(e.getPositionEnum) }
      .filter { bu => predicate(bu.getPlayer) }
      .map(score(_, vs))
      .sum
  }

  def score(bu: Backup, vs: VsHand): Double = {
    val base = InLineupScore(bu.getPlayer, bu.getPosition, vs).total * bu.getPercentage
    val fatigue = (bu.getPercentage * Defense.getPositionFactor(bu.getPosition) * bu.getPercentage / 10) / 2

    (base - fatigue) / 100.0

  }
}

