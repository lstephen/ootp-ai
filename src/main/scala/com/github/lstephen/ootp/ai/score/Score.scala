package com.github.lstephen.ootp.ai.score

import scalaz.Foldable

import spire.algebra.Order

class Score(private val n: Double) extends AnyVal {
  def toDouble: Double = n
  def toLong: Long = n.round

  def compare(that: Score) = n compare that.n

  def +(that: Score) = new Score(n + that.n)
}

class ScoreIsOrdered extends Order[Score] {
  def compare(x: Score, y: Score): Int = x compare y
}

object Score {
  val zero = Score(0)
  def apply[N: Numeric](n: N): Score = new Score(implicitly[Numeric[N]] toDouble n)

  implicit val order: Order[Score] = new ScoreIsOrdered

  implicit class FoldableOfScore[F[_]: Foldable](xs: F[Score]) {
    import scalaz.Scalaz._

    def total: Score = xs.foldLeft(zero)(_ + _)
  }
}

