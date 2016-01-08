package com.github.lstephen.ootp.ai.score

import scala.collection.GenTraversableOnce

class Score(private val n: Double) extends AnyVal with Ordered[Score] {
  def toDouble: Double = n
  def toLong: Long = n.round

  def compare(that: Score) = n compare that.n

  def +(that: Score) = new Score(n + that.n)
}

object Score {
  def apply[N: Numeric](n: N): Score = new Score(implicitly[Numeric[N]] toDouble n)

  val zero: Score = Score(0)

  implicit class TraversableOfScore(xs: GenTraversableOnce[Score]) {
    def total = xs.foldLeft(zero)(_ + _)
  }
}

