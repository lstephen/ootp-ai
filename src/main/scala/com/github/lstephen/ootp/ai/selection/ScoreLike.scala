package com.github.lstephen.ootp.ai.selection

import com.github.lstephen.ootp.ai.algebra._

import scalaz.{ Order => _, _ }
import scalaz.syntax.traverse._

import spire.algebra._

trait ScoreLike {
  def total: Score
}

object ScoreLike {
  implicit def ord[A <: ScoreLike]: Order[A] = Order.by(_.total)

  implicit class FoldableOfScoreLike[A <: ScoreLike, T[_]: Traverse](xs: T[A]) {
    def total: Score = xs.map(_.total).msum
  }
}

