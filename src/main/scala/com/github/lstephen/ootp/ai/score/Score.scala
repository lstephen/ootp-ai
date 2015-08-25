package com.github.lstephen.ootp.ai.score

import scalaz.Foldable

import spire.algebra.{ AdditiveMonoid, Order }

class Score(private val n: Double) extends AnyVal {
  def toDouble: Double = n
  def toLong: Long = n.round

  def compare(that: Score) = n compare that.n

  def +(that: Score) = new Score(n + that.n)
}

trait ScoreIsOrdered extends Order[Score] {
  def compare(x: Score, y: Score): Int = x compare y
}

trait ScoreIsAdditiveMonoid extends AdditiveMonoid[Score] {
  val zero: Score = Score(0)
  def plus(x: Score, y: Score): Score = x + y
}

class ScoreAlgebra extends ScoreIsOrdered with ScoreIsAdditiveMonoid

object Score {
  def apply[N: Numeric](n: N): Score = new Score(implicitly[Numeric[N]] toDouble n)

  implicit val algebra = new ScoreAlgebra
}

