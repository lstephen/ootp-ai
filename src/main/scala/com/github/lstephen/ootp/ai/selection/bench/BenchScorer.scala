package com.github.lstephen.ootp.ai.selection.bench

import com.github.lstephen.ootp.ai.player.Player
import com.github.lstephen.ootp.ai.player.ratings.Position
import com.github.lstephen.ootp.ai.regression.Predictor
import com.github.lstephen.ootp.ai.score.Score
import com.github.lstephen.ootp.ai.selection.depthchart.DepthChart.Backup
import com.github.lstephen.ootp.ai.selection.depthchart.DepthChartSelection
import com.github.lstephen.ootp.ai.selection.lineup.Defense
import com.github.lstephen.ootp.ai.selection.lineup.InLineupScore
import com.github.lstephen.ootp.ai.selection.lineup.Lineup
import com.github.lstephen.ootp.ai.selection.lineup.Lineup.VsHand

import collection.JavaConversions._

class BenchScorer(implicit predictor: Predictor) {
  val depthChartSelection = new DepthChartSelection

  def score(bench: java.lang.Iterable[Player],
            lineup: Lineup,
            vs: VsHand): Double = {
    score_(bench, lineup, vs).toDouble
  }

  def score_(bench: Traversable[Player], lineup: Lineup, vs: VsHand): Score = {
    val dc = depthChartSelection.select(lineup, bench.toSet, vs)

    lineup
      .filter(_.getPositionEnum != Position.PITCHER)
      .flatMap(bu => dc.getBackups(bu.getPositionEnum))
      .map(score(_, vs))
      .total
  }

  def score(bu: Backup, vs: VsHand): Score =
    (Defense.getPositionFactor(bu.getPosition) * bu.getPercentage / 100.0) *: InLineupScore(
      bu.getPlayer,
      bu.getPosition,
      vs).score

}
