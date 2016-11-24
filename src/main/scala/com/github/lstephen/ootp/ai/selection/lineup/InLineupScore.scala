package com.github.lstephen.ootp.ai.selection.lineup

import com.github.lstephen.ootp.ai.player.Player
import com.github.lstephen.ootp.ai.player.ratings.Position
import com.github.lstephen.ootp.ai.regression.Predictor
import com.github.lstephen.ootp.ai.score._
import com.github.lstephen.ootp.ai.selection.lineup.Lineup.VsHand

import scalaz._
import Scalaz._

import collection.JavaConversions._

class InLineupScore(player: Player,
                    position: Position,
                    vs: Option[VsHand] = None)(implicit predictor: Predictor)
    extends Scoreable {

  val name = player.getShortName

  val hitting: Score = vs match {
    case None => predictor.predictBatting(player).overall
    case Some(vs) => Score(vs.getStats(predictor, player).getWobaPlus)
  }

  val defense = new PlayerDefenseScore(player, position)

  val score = hitting + defense.score
}

object InLineupScore {
  def apply(ply: Player)(implicit ps: Predictor): InLineupScore =
    Position.hitting.map(InLineupScore(ply, _)).max

  def apply(ply: Player, pos: Position)(
      implicit ps: Predictor): InLineupScore = {
    new InLineupScore(ply, pos, None)
  }

  def apply(ply: Player, pos: Position, vs: VsHand)(
      implicit ps: Predictor): InLineupScore = {
    new InLineupScore(ply, pos, Some(vs))
  }

  def sort(players: Traversable[Player], pos: Position, vs: VsHand)(
      implicit ps: Predictor): Seq[Player] = {
    players.toSeq.sortBy(InLineupScore(_, pos, vs)).reverse
  }
}
