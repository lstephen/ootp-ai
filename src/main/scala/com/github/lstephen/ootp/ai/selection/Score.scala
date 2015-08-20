package com.github.lstephen.ootp.ai.selection

import spire.algebra._
import spire.math._
import spire.implicits._

class Score(private[Score] val n: Score.N) {
  def toInt: Int = n.round.intValue
  def toDouble: Double = n.doubleValue

  def compare(rhs: Score) = n compare rhs.n

  def +(rhs: Score) = Score(n + rhs.n)
}

trait ScoreIsAdditiveCMonoid extends AdditiveCMonoid[Score] {
  def zero: Score = Score(0)
  def plus(lhs: Score, rhs: Score): Score = lhs + rhs
}

trait ScoreIsOrdered extends Order[Score] {
  def compare(x: Score, y: Score): Int = x compare y
}

class ScoreAlgebra extends ScoreIsAdditiveCMonoid with ScoreIsOrdered

object Score {
  private[Score] type N = Double

  def apply(n: N): Score = new Score(n)

  implicit val Alg: ScoreAlgebra = new ScoreAlgebra
}

