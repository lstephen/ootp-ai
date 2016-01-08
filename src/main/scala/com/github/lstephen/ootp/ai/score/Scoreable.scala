package com.github.lstephen.ootp.ai.score

trait Scoreable {
  def toDouble: Double = score.toDouble
  def toLong: Long = score.toLong

  def score: Score

  def compare(that: Scoreable): Int = score compare that.score
}

object Scoreable {
  implicit def ordering[A <: Scoreable]: Ordering[A] = Ordering.by(_.score)

  implicit class TraversableOfScoreable[A <: Scoreable](xs: Iterable[A]) {
    def total: Score = xs.map(_.score).total
  }
}

