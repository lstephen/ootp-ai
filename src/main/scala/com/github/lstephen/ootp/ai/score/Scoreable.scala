package com.github.lstephen.ootp.ai.score

import scala.math.{ Ordering => ScalaOrdering }

import scalaz._
import Scalaz._

trait Scoreable {
  def score: Score

  def toDouble: Double = score.toDouble
  def toLong: Long = score.toLong
}

class ScoreableIsOrdered[A <: Scoreable](implicit o: Order[Double]) extends Order[A] {
  def order(x: A, y: A): Ordering = x.score ?|? y.score
}

object Scoreable {
  implicit def ordering[A <: Scoreable]: ScalaOrdering[A] = new ScoreableIsOrdered().toScalaOrdering

  implicit class TraverseOfScoreable[A <: Scoreable, T[_]: Traverse](xs: T[A]) {
    import Score.FoldableOfScore
    def total: Score = xs.map(_.score).total
  }
}

