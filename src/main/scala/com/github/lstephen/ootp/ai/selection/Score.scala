package com.github.lstephen.ootp.ai.selection

import com.github.lstephen.ootp.ai.algebra._

import spire.algebra.{ CMonoid, Order }
import spire.math.Real

import spire.implicits._

class Score(private[Score] val n: Real) {
  def toInt: Int = n.round.intValue
  def toDouble: Double = n.doubleValue

  def compare(rhs: Score) = n compare rhs.n

  def +(rhs: Score) = Score(n + rhs.n)
}

trait ScoreIsMonoid extends CMonoid[Score] {
  def id: Score = Score(0)
  def op(lhs: Score, rhs: Score): Score = lhs + rhs
}

trait ScoreIsOrdered extends Order[Score] {
  def compare(x: Score, y: Score): Int = x compare y
}

class ScoreAlgebra extends ScoreIsMonoid with ScoreIsOrdered

object Score {
  def apply(t: Real): Score = new Score(t)

  implicit val Alg: ScoreAlgebra = new ScoreAlgebra
}

trait ScoreLike {
  def total: Score
}

object ScoreLike {
  implicit def ord[A <: ScoreLike]: Order[A] = Order.by(_.total)

  implicit class ScoreLikeSeq[A <: ScoreLike](xs: Seq[A]) {
    def total: Score = xs.map(_.total).mconcat
  }
}


