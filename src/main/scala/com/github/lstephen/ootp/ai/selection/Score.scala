package com.github.lstephen.ootp.ai.selection

import scala.math.{ Ordering => ScalaOrdering }

import scalaz._
import Scalaz._

trait Score {
  def total: Double

  def +(that: Score): Score = Score(total + that.total)
}

class ScoreIsOrdered[A <: Score](implicit o: Order[Double]) extends Order[A] {
  def order(x: A, y: A): Ordering = x.total ?|? y.total
}

object Score {
  private class SimpleScore(val total: Double) extends Score

  def apply(t: Double): Score = new SimpleScore(t)

  implicit def ordering[A <: Score]: ScalaOrdering[A] = new ScoreIsOrdered().toScalaOrdering

  implicit class FoldableOfScore[A <: Score, F[_]: Foldable](xs: F[A]) {
    def total: Score = Score(xs.foldLeft(0)(_.total + _.total))
  }
}

