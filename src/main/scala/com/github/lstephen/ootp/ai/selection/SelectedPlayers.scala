package com.github.lstephen.ootp.ai.selection

import com.github.lstephen.ootp.ai.player.Player
import com.github.lstephen.ootp.ai.player.ratings.Position
import com.github.lstephen.ootp.ai.regression.Predictor
import com.github.lstephen.ootp.ai.score._
import com.github.lstephen.ootp.ai.selection.bench.BenchScorer
import com.github.lstephen.ootp.ai.selection.lineup.InLineupScore
import com.github.lstephen.ootp.ai.selection.lineup.Lineup
import com.github.lstephen.ootp.ai.selection.lineup.Lineup.VsHand
import com.github.lstephen.ootp.ai.selection.lineup.LineupSelection
import com.github.lstephen.ootp.ai.stats.SplitPercentages

import collection.JavaConversions._

import scalaz._
import Scalaz._

class SelectedPlayers(players: Set[Player])(implicit predictor: Predictor,
                                            splits: SplitPercentages) {

  def score: Double = {
    val lineups = new LineupSelection(predictor).select(players)

    splits.getVsRhpPercentage() * score(VsHand.VS_RHP,
                                        lineups.getVsRhpPlusDh()) +
      splits.getVsRhpPercentage() * score(VsHand.VS_RHP, lineups.getVsRhp()) +
      splits.getVsLhpPercentage() * score(VsHand.VS_LHP,
                                          lineups.getVsLhpPlusDh()) +
      splits.getVsLhpPercentage() * score(VsHand.VS_LHP, lineups.getVsLhp())
  }

  def score(vs: VsHand, lineup: Lineup): Double =
    lineupScore(lineup, vs).toDouble + benchScore(lineup, vs)

  def benchScore(lineup: Lineup, vs: VsHand): Double =
    new BenchScorer().score(players -- lineup.playerSet, lineup, vs)

  def lineupScore(lineup: Lineup, vs: VsHand): Score =
    lineup
      .filter(_.getPositionEnum != Position.PITCHER)
      .map(e => InLineupScore(e.getPlayer, e.getPositionEnum, vs))
      .toList
      .total
}

object SelectedPlayers {
  def create(players: java.lang.Iterable[Player],
             predictor: Predictor,
             splits: SplitPercentages): SelectedPlayers =
    new SelectedPlayers(Set() ++ players)(predictor, splits)
}
