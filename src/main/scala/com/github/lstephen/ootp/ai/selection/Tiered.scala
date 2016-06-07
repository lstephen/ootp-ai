package com.github.lstephen.ootp.ai.selection

import com.github.lstephen.ootp.ai.player.Player
import com.github.lstephen.ootp.ai.player.ratings.Position
import com.github.lstephen.ootp.ai.regression.Predictor
import com.github.lstephen.ootp.ai.value.NowAbility

import scala.collection.JavaConverters._
import collection.JavaConversions._

class Tiered(tiers: java.lang.Iterable[Position])(implicit predictor: Predictor) {

  def first(ps: List[Player]): List[Player] =
    tiers
      .toList
      .map(pos => ps.maxBy(NowAbility(_, pos)))
      .distinct
      .sortBy(NowAbility(_))

  def group(ps: List[Player]): List[List[Player]] = {
    var result: List[List[Player]] = List()

    while (result.flatten.size < ps.size) {
      result = result :+ first(ps diff result.flatten)
    }

    result
  }

  def sort(ps: List[Player]): List[Player] = group(ps).flatten
  def take(ps: List[Player], n: Int): List[Player] = sort(ps).take(n)

  def takeAsJava(ps: java.lang.Iterable[Player], n: Int) = take(ps.toList, n).asJava
}

