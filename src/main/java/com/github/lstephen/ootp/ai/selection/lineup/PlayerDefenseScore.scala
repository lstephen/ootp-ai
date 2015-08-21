package com.github.lstephen.ootp.ai.selection.lineup

import com.github.lstephen.ootp.ai.player.Player
import com.github.lstephen.ootp.ai.player.ratings.{ DefensiveRatings, Position }
import com.github.lstephen.ootp.ai.selection.Score

class PlayerDefenseScore(defensiveRatings: DefensiveRatings, position: Position)
  extends Score {

  def this(ply: Player, pos: Position) = this(ply.getDefensiveRatings, pos)

  val positionFactor = Defense.getPositionFactor(position)
  val score = defensiveRatings getPositionScore position

  val total = positionFactor * score
}

object PlayerDefenseScore {
  def atBestPosition(p: Player): PlayerDefenseScore =
    Position.values
      .map(new PlayerDefenseScore(p, _))
      .max
}

