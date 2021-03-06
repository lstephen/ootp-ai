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

import java.io.PrintWriter

class ReplacementLevels(levels: Map[Position, Score])(implicit ps: Predictor)
    extends Printable {
  private def get(pos: Position): Score =
    levels.get(pos).getOrElse(throw new IllegalStateException())

  def get(ply: Player, pos: Position): Score = get(NowAbility(ply, pos))

  def get(a: Ability): Score = getVs(a, get(a.position))

  val average = (Position.hitting() ++ Position.pitching()).map(get(_)).average

  def getVsAverage(a: Ability): Score = getVs(a, average)

  def getVs(a: Ability, level: Score) =
    if (a.position == Position.MIDDLE_RELIEVER)
      0.865 *: (a.score - level)
    else
      a.score - level

  def getTrendingTowardsAverage(a: Ability) =
    List(get(a), getVsAverage(a)).average

  def print(w: PrintWriter): Unit = {
    def printLevel(p: Position): Unit =
      w.println(
        f"${p.getAbbreviation}%2s: ${get(p).toLong}%3d ${MaxLevels.getForIdeal(p).toLong}%3d")

    w.println
    Position.hitting.foreach(printLevel(_))
    w.println
    Position.pitching.foreach(printLevel(_))
  }
}

object ReplacementLevels {
  private def shouldNotHappen = throw new IllegalStateException()

  private var _forIdeal: Option[ReplacementLevels] = None

  def getForIdeal(implicit ps: Predictor): ReplacementLevels =
    _forIdeal.getOrElse {
      _forIdeal = Some(getFor(Context.idealRoster.getOrElse(shouldNotHappen)))
      _forIdeal getOrElse shouldNotHappen
    }

  def getFor(r: Roster)(implicit ps: Predictor): ReplacementLevels = {
    new ReplacementLevels(
      (Position.hitting ++ Position.pitching)
        .foldLeft(Map.empty[Position, Score])((m, pos) =>
          m + (pos -> top(r)(NowAbility(_, pos).score))))
  }

  def top[S](r: Roster)(f: Player => S)(implicit ps: Predictor,
                                        ord: Ordering[S]): S =
    r.getMinorLeaguers.toList.map(f).sorted.reverse.head
}

object MaxLevels {
  def getVsIdeal(a: Ability): Score =
    if (a.position == Position.MIDDLE_RELIEVER)
      0.865 *: (a.score - getForIdeal(a))
    else
      a.score - getForIdeal(a)

  def getVsFuture(a: Ability): Score =
    if (a.position == Position.MIDDLE_RELIEVER)
      0.865 *: (a.score - getForFuture(a))
    else
      a.score - getForFuture(a)

  def getVsAverage(a: Ability): Score =
    if (a.position == Position.MIDDLE_RELIEVER)
      0.865 *: (a.score - average)
    else
      a.score - average

  def getForIdeal(a: Ability): Score = getForIdeal(a.position, Some(a.player))

  def getForIdeal(pos: Position, ply: Option[Player] = None): Score =
    getFor(
      Context.idealRoster.getOrElse(throw new IllegalStateException),
      pos,
      ply)(Context.currentPredictor.getOrElse(throw new IllegalStateException))

  val average =
    (Position.hitting() ++ Position.pitching()).map(getForIdeal(_)).average

  def getFor(r: Roster, pos: Position, ply: Option[Player] = None)(
      implicit ps: Predictor): Score =
    getFor(r, pos, ply, NowAbility(_, pos).score)

  def getForFuture(a: Ability): Score =
    getForFuture(
      Context.idealRoster.getOrElse(throw new IllegalStateException),
      a.position,
      Some(a.player))(
      Context.currentPredictor.getOrElse(throw new IllegalStateException))

  def getForFuture(r: Roster, pos: Position, ply: Option[Player] = None)(
      implicit ps: Predictor): Score =
    getFor(r, pos, ply, FutureAbility(_, pos).score)

  def getFor(r: Roster,
             pos: Position,
             ply: Option[Player],
             a: Player => Score)(implicit ps: Predictor): Score =
    r.getAllPlayers.toList
      .filter(p => ply.map(_ != p).getOrElse(true))
      .map(a(_))
      .max
}
