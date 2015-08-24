package com.github.lstephen.ootp.ai.selection

import scala.math.{ Ordering => ScalaOrdering }

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

class ScoreableIsOrdered[A <: Scoreable](implicit o: Order[Double]) extends Order[A] {
  def order(x: A, y: A): Ordering = x.score ?|? y.score
}

object Score {
  val zero = Score(0)
  def apply[N: Numeric](n: N): Score = new Score(implicitly[Numeric[N]] toDouble n)

  implicit def order: Order[Score] = new ScoreIsOrdered

  implicit class FoldableOfScore[F[_]: Foldable](xs: F[Score]) {
    def total: Score = xs.foldLeft(zero)(_ + _)
  }

}

trait Scoreable {
  def score: Score

  def toDouble: Double = score.toDouble
  def toLong: Long = score.toLong
}

object Scoreable {
  implicit def ordering[A <: Scoreable]: ScalaOrdering[A] = new ScoreableIsOrdered().toScalaOrdering

  implicit class TraverseOfScoreable[A <: Scoreable, T[_]: Traverse](xs: T[A]) {
    import Score.FoldableOfScore
    def total: Score = xs.map(_.score).total
  }
}

