package com.github.lstephen.ootp.ai.selection.lineup

import com.github.lstephen.ootp.ai.WithAnyRoster
import com.github.lstephen.ootp.ai.player.Player
import com.github.lstephen.ootp.ai.player.ratings.{ DefensiveRatings, Position }
import com.github.lstephen.ootp.ai.score._

import collection.JavaConversions._

class PlayerDefenseScore(defensiveRatings: DefensiveRatings, position: Position, useBaseline: Boolean = true)
  extends Scoreable with WithAnyRoster {

  def this(ply: Player, pos: Position) = this(ply.getDefensiveRatings, pos)

  val positionFactor = Defense.getPositionFactor(position)
  val positionScore = defensiveRatings getPositionScore position

  val baseline = if (useBaseline) PlayerDefenseScore.baseline(roster.getAllPlayers) else Score.zero

  val score = Score(positionFactor * positionScore) - baseline
}

object PlayerDefenseScore {
  def atBestPosition(p: Player, useBaseline: Boolean = true): PlayerDefenseScore =
    Position.values
      .map(new PlayerDefenseScore(p.getDefensiveRatings, _, useBaseline))
      .max

  def baseline(ps: Traversable[Player]): Score = {
    ps.filter(_.isHitter).map(atBestPosition(_, false).score).average
  }
}

