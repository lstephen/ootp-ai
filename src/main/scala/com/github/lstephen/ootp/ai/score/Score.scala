package com.github.lstephen.ootp.ai.score

import scala.collection.GenTraversableOnce

class Score(private val n: Double) extends AnyVal with Ordered[Score] {
  def toDouble: Double = n
  def toLong: Long = n.round

  def compare(that: Score): Int = n compare that.n

  def +(that: Score): Score = Score(n + that.n)
  def -(that: Score): Score = Score(n - that.n)
  def unary_-(): Score = Score(-n)
  def *:[N: Numeric](that: N): Score = Score(implicitly[Numeric[N]].toDouble(that) * n)
  def :/[N: Numeric](that: N): Score = Score(n / implicitly[Numeric[N]].toDouble(that))
}

object Score {
  def apply[N: Numeric](n: N): Score = new Score(implicitly[Numeric[N]] toDouble n)

  // Java Interop
  def apply(n: Number): Score = Score(n.doubleValue)

  val zero: Score = Score(0)

  implicit class TraversableOfScore(xs: GenTraversableOnce[Score]) {
    def total = xs.foldLeft(zero)(_ + _)
  }
}

