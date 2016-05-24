package com.github.lstephen.ootp.ai.value

import com.github.lstephen.ootp.ai.Context
import com.github.lstephen.ootp.ai.io.Printable
import com.github.lstephen.ootp.ai.player.Player
import com.github.lstephen.ootp.ai.player.ratings.Position
import com.github.lstephen.ootp.ai.regression.Predictor
import com.github.lstephen.ootp.ai.roster.Roster
import com.github.lstephen.ootp.ai.score.Score
import com.github.lstephen.ootp.ai.selection.lineup.InLineupScore

import collection.JavaConversions._

import java.io.PrintWriter;


class ReplacementLevels(levels: Map[Position, Score])(implicit ps: Predictor) extends Printable {
  def get(pos: Position): Score =
    levels.get(pos).getOrElse(throw new IllegalStateException())

   def get(ply: Player, pos: Position): Score =
     NowAbility(ply, pos).score - get(pos)
    // TODO: MR value using bullpen chaining

  def print(w: PrintWriter): Unit = {
    def printLevel(p: Position): Unit
      = w.println(f"${p.getAbbreviation}%2s: ${get(p).toLong}%3d")

    Position.hitting.foreach(printLevel(_))
    Position.pitching.foreach(printLevel(_))
  }
}

object ReplacementLevels {
  private def shouldNotHappen = throw new IllegalStateException()

  private var _forNow: Option[ReplacementLevels] = None

  def getForNow(implicit ps: Predictor): ReplacementLevels =
    _forNow.getOrElse {
      _forNow = Some(getFor(Context.newRoster.getOrElse(shouldNotHappen)))
      _forNow getOrElse shouldNotHappen
    }

  def getFor(r: Roster)(implicit ps: Predictor): ReplacementLevels = {
    new ReplacementLevels(
      (Position.hitting ++ Position.pitching).foldLeft(Map.empty[Position, Score])(
        (m, pos) => m + (pos -> top(r)(NowAbility(_, pos).score))
      ))
  }

  def top[S](r: Roster)(f: Player => S)(implicit ps: Predictor, ord: Ordering[S]): S =
    r.getMinorLeaguers
      .toList
      .map(f)
      .sorted
      .reverse
      .head
}

