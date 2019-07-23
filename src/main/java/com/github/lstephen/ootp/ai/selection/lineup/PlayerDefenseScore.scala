package com.github.lstephen.ootp.ai.selection.lineup

import com.github.lstephen.ootp.ai.Context
import com.github.lstephen.ootp.ai.player.Player
import com.github.lstephen.ootp.ai.player.ratings.{DefensiveRatings, Position}
import com.github.lstephen.ootp.ai.score._
import scala.collection.mutable.Map
import collection.JavaConversions._

class PlayerDefenseScore(defensiveRatings: DefensiveRatings,
                         val position: Position,
                         useBaseline: Boolean = true)
    extends Scoreable {

  def this(ply: Player, pos: Position) = this(ply.getDefensiveRatings, pos)

  val positionFactor = Defense.getPositionFactor(position)
  val positionScore = defensiveRatings getPositionScore position

  val baseline = if (useBaseline) PlayerDefenseScore.baseline(position) else Score.zero

  val score = Score(positionFactor * positionScore) - baseline
}

object PlayerDefenseScore {
  def atBestPosition(p: Player,
                     useBaseline: Boolean = true): PlayerDefenseScore =
    Position.hitting
      .map(new PlayerDefenseScore(p.getDefensiveRatings, _, useBaseline))
      .max

  val oldRosterBaseline: Map[Position, Score] = Map()
  val newRosterBaseline: Map[Position, Score] = Map()

  def baseline(pos: Position): Score = {
    newRosterBaseline.get(pos).getOrElse {
      Context.newRoster match {
        case Some(r) =>
          newRosterBaseline.put(pos, calculateBaseline(r.getAllPlayers, pos))
          newRosterBaseline.get(pos).getOrElse(throw new IllegalStateException)
        case None =>
          oldRosterBaseline.get(pos).getOrElse {
            Context.oldRoster match {
              case Some(r) =>
                oldRosterBaseline.put(pos, calculateBaseline(r.getAllPlayers, pos))
                oldRosterBaseline.get(pos).getOrElse(throw new IllegalStateException)
              case None => throw new RuntimeException("No rosters set")
            }
          }
      }
    }
  }

  def calculateBaseline(ps: Traversable[Player], pos: Position): Score = {
    ps.filter(_.isHitter).map(atBestPosition(_, false)).filter(_.position == pos).map(_.score).average
  }
}
