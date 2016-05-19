package com.github.lstephen.ootp.ai.selection.lineup

import com.github.lstephen.ootp.ai.Context
import com.github.lstephen.ootp.ai.player.Player
import com.github.lstephen.ootp.ai.player.ratings.{ DefensiveRatings, Position }
import com.github.lstephen.ootp.ai.score._

import collection.JavaConversions._

class PlayerDefenseScore(defensiveRatings: DefensiveRatings, position: Position, useBaseline: Boolean = true)
  extends Scoreable {

  def this(ply: Player, pos: Position) = this(ply.getDefensiveRatings, pos)

  val positionFactor = Defense.getPositionFactor(position)
  val positionScore = defensiveRatings getPositionScore position

  val baseline = if (useBaseline) PlayerDefenseScore.baseline else Score.zero

  val score = Score(positionFactor * positionScore) - baseline
}

object PlayerDefenseScore {
  def atBestPosition(p: Player, useBaseline: Boolean = true): PlayerDefenseScore =
    Position.values
      .map(new PlayerDefenseScore(p.getDefensiveRatings, _, useBaseline))
      .max

  var oldRosterBaseline: Option[Score] = None
  var newRosterBaseline: Option[Score] = None

  def baseline: Score = {
    newRosterBaseline.getOrElse {
      Context.newRoster match {
        case Some(r) =>
          newRosterBaseline = Some(calculateBaseline(r.getAllPlayers))
          newRosterBaseline.getOrElse(throw new IllegalStateException)
        case None   => oldRosterBaseline.getOrElse {
          Context.oldRoster match {
            case Some(r) =>
              oldRosterBaseline = Some(calculateBaseline(r.getAllPlayers))
              oldRosterBaseline.getOrElse(throw new IllegalStateException)
            case None    => throw new RuntimeException("No rosters set")
          }
        }
      }
    }
  }

  def calculateBaseline(ps: Traversable[Player]): Score = {
    ps.filter(_.isHitter).map(atBestPosition(_, false).score).average
  }
}

