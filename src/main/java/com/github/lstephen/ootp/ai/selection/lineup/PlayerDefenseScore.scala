package com.github.lstephen.ootp.ai.selection.lineup

import com.github.lstephen.ootp.ai.player.Player
import com.github.lstephen.ootp.ai.player.ratings.{ DefensiveRatings, Position }
import com.github.lstephen.ootp.ai.score._

class PlayerDefenseScore(defensiveRatings: DefensiveRatings, position: Position)
  extends Scoreable {

  def this(ply: Player, pos: Position) = this(ply.getDefensiveRatings, pos)

  val positionFactor = Defense.getPositionFactor(position)
  val positionScore = defensiveRatings getPositionScore position

  val score = Score(positionFactor * positionScore)
}

object PlayerDefenseScore {
  def atBestPosition(p: Player): PlayerDefenseScore =
    Position.values
      .map(new PlayerDefenseScore(p, _))
      .max
}

