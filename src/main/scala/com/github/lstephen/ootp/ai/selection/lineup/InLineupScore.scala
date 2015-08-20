package com.github.lstephen.ootp.ai.selection.lineup

import com.github.lstephen.ootp.ai.player.Player
import com.github.lstephen.ootp.ai.player.ratings.Position
import com.github.lstephen.ootp.ai.regression.Predictions
import com.github.lstephen.ootp.ai.selection.Score
import com.github.lstephen.ootp.ai.selection.ScoreLike
import com.github.lstephen.ootp.ai.selection.lineup.Lineup.VsHand

import spire.compat._

class InLineupScore
  (player: Player, position: Position, vs: Option[VsHand] = None)
  (implicit predictions: Predictions)
  extends ScoreLike {

  val name = player.getShortName

  val hitting = vs match {
    case None     => predictions.getOverallHitting(player)
    case Some(vs) => predictions.getHitting(player, vs)
  }

  val defense = new PlayerDefenseScore(player, position).total

  def total = Score(hitting + defense)
}

object InLineupScore {
  def apply(ply: Player)(implicit ps: Predictions): InLineupScore = {
    Position.values
      .map(InLineupScore(ply, _))
      .max
  }


  def apply(ply: Player, pos: Position)(implicit ps: Predictions): InLineupScore = {
    new InLineupScore(ply, pos, None)
  }

  def apply(ply: Player, pos: Position, vs: VsHand)(implicit ps: Predictions): InLineupScore = {
    new InLineupScore(ply, pos, Some(vs))
  }

  def sort(players: Traversable[Player], pos: Position, vs: VsHand)(implicit ps: Predictions): Seq[Player] = {
    players.toSeq.sortBy(InLineupScore(_, pos, vs)).reverse
  }
}

