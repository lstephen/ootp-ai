package com.github.lstephen.ootp.ai

import scalaz._
import scalaz.syntax.foldable._

import spire.algebra._
import spire.implicits._

object algebra {
  implicit class FoldableOfAdditiveMonoid[A: AdditiveMonoid, F[_]: Foldable](xs: F[A]) {
    def msum: A = xs.foldLeft(implicitly[AdditiveMonoid[A]].zero)(_ + _)
  }
}

