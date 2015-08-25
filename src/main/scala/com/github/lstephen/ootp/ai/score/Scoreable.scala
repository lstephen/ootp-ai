package com.github.lstephen.ootp.ai.score

import scala.math.Ordering

import scalaz.Traverse
import scalaz.syntax.TraverseSyntax

import spire.algebra.Order

trait Scoreable {
  def toDouble: Double = score.toDouble
  def toLong: Long = score.toLong

  def score: Score
}

object Scoreable {
  implicit def order[A <: Scoreable]: Order[A] = Order.by(_.score)
  implicit def ordering[A <: Scoreable]: Ordering[A] = Order.ordering

  implicit class TraverseOfScoreable[A <: Scoreable, T[_]: Traverse](xs: T[A]) {
    import Score._
    import scalaz.Scalaz._

    def total: Score = xs.map(_.score).total
  }
}

