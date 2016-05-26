package com.github.lstephen.ootp.ai.score

import scala.collection.GenTraversableOnce

class Score(private val n: Double) extends AnyVal with Ordered[Score] {
  def toDouble: Double = n
  def toLong: Long = n.round

  def compare(that: Score) = n compare that.n

  def isPositive = n > 0.0

  def +(that: Score) = Score(n + that.n)
  def -(that: Score) = Score(n - that.n)

  def *:[N: Numeric](that: N): Score = Score(implicitly[Numeric[N]].toDouble(that) * n)
  def :/(that: Int) = Score(n / that)
}

object Score {
  def apply[N: Numeric](n: N): Score = new Score(implicitly[Numeric[N]] toDouble n)
  def apply(n: Number): Score = new Score(n.doubleValue)

  val zero: Score = Score(0)

  def max(lhs: Score, rhs: Score) = if (lhs.n > rhs.n) lhs else rhs

  implicit class TraversableOfScore(xs: GenTraversableOnce[Score]) {
    def total = xs.foldLeft(zero)(_ + _)
    def average = total :/ xs.size
  }

  implicit class OptionOfScore(xs: Option[Score]) {
    def orElseZero = xs.getOrElse(Score.zero)
  }

  implicit class TraversableOfOptionScore(xs: GenTraversableOnce[Option[Score]]) {
    def total = xs.foldLeft(zero)(_ + _.orElseZero)
  }
}

