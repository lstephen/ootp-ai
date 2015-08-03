package com.github.lstephen.ootp.ai.selection.bench

import com.github.lstephen.ootp.ai.player.Player
import com.github.lstephen.ootp.ai.player.ratings.Position
import com.github.lstephen.ootp.ai.regression.Predictions
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
    var score = 0.0

    for (entry <- lineup if entry.getPositionEnum != Position.PITCHER;
        (p, i) <- selectBenchPlayers(bench, entry.getPositionEnum, vs).zipWithIndex) {
      if (predicate(p)) {
        score +=
          InLineupScore(p, entry.getPositionEnum, vs).total *
            expectedPlayingTime(entry.getPositionEnum, i + 1)
      }
    }

    score
  }

  def expectedPlayingTime(p: Position, rank: Integer): Double = {
    math.pow(0.04 * Defense.getPositionFactor(p), rank.toDouble)
  }

  def selectBenchPlayers(bench: Traversable[Player], position: Position, vs: VsHand): Seq[Player] = {
    bench
      .toSeq
      .sortBy(InLineupScore(_, position, vs).total)
      .reverse
      .take(2)
  }

}

