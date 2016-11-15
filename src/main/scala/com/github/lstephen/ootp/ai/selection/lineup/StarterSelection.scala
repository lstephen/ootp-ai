package com.github.lstephen.ootp.ai.selection.lineup

import com.github.lstephen.ootp.ai.player.Player
import com.github.lstephen.ootp.ai.player.ratings.Position
import com.github.lstephen.ootp.ai.regression.Predictor
import com.github.lstephen.ootp.ai.score._
import com.github.lstephen.ootp.ai.selection.lineup.Lineup.VsHand
import com.github.lstephen.ootp.ai.search._
import com.github.lstephen.ootp.ai.syntax._

import scala.collection.GenTraversableOnce
import scala.collection.JavaConversions._

import java.lang.{ Iterable => JavaIterable }

class StarterSelection(implicit predictor: Predictor) {

  type Starters = Map[Position, Player]

  def selectWithDh(ps: GenTraversableOnce[Player], vs: VsHand): Set[Player] =
      selectForPositions(Position.hitting.toSet, ps, vs)

  def select(ps: GenTraversableOnce[Player], vs: VsHand): Set[Player] =
      selectForPositions(Position.hitting.toSet - Position.DESIGNATED_HITTER, ps, vs)

  def selectForPositions
    (pos: Set[Position], ps: GenTraversableOnce[Player], vs: VsHand): Set[Player] = {

    val initial = (pos.toList, ps.toList.sortBy(InLineupScore(_)).reverse).zipped.toMap

    new HillClimbing(heuristic(vs), navigator(ps)).search(initial).values.toSet
  }

  def heuristic(vs: VsHand): Starters => Score =
    _.map { case (pos, ply) => InLineupScore(ply, pos, vs) }.total

  def navigator(ps: GenTraversableOnce[Player]): Starters => List[Starters] =
    swaps ++ subs(ps) ++ subsAndSwaps(ps)

  def swaps: Starters => List[Starters] =
    s => (for (x <- s.keys; y <- s.keys) yield s.swap(x, y)).toList

  def subs(ps: GenTraversableOnce[Player]): Starters => List[Starters] =
    s => ps.foldLeft(List[Starters]())((ls, p) => ls ++ subs(s, p))

  def subsAndSwaps(ps: GenTraversableOnce[Player]): Starters => List[Starters] =
    subs(ps) flatMap swaps

  def subs(s: Starters, p: Player): List[Starters] = {
    s.values.find(_ == p) match {
      case Some(_) => List()
      case None    => (for ((k, v) <- s) yield s + (k -> p)).toList
    }
  }


  // For Java compat
  def select(vs: VsHand, ps: JavaIterable[Player]): JavaIterable[Player] =
    select(List() ++ ps, vs)

  def selectWithDh(vs: VsHand, ps: JavaIterable[Player]): JavaIterable[Player] =
    selectWithDh(List() ++ ps, vs)

}



