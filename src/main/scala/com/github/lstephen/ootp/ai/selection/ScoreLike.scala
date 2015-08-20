package com.github.lstephen.ootp.ai.selection

import com.github.lstephen.ootp.ai.algebra._

import spire.algebra.Order

trait ScoreLike {
  def total: Score
}

object ScoreLike {
  implicit def ord[A <: ScoreLike]: Order[A] = Order.by(_.total)

  implicit class ScoreLikeSeq[A <: ScoreLike](xs: Seq[A]) {
    def total: Score = xs.map(_.total).mconcat
  }
}

