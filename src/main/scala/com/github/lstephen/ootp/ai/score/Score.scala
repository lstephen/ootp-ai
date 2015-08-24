package com.github.lstephen.ootp.ai.score

import scalaz._
import Scalaz._

class Score(private val n: Double) extends AnyVal {
  def toDouble: Double = n
  def toLong: Long = n.round

  def +(that: Score) = new Score(n + that.n)
}


class ScoreIsOrdered(implicit o: Order[Double]) extends Order[Score] {
  def order(x: Score, y: Score): Ordering = x.toDouble ?|? y.toDouble
}

object Score {
  val zero = Score(0)
  def apply[N: Numeric](n: N): Score = new Score(implicitly[Numeric[N]] toDouble n)

  implicit def order: Order[Score] = new ScoreIsOrdered

  implicit class FoldableOfScore[F[_]: Foldable](xs: F[Score]) {
    def total: Score = xs.foldLeft(zero)(_ + _)
  }

}

