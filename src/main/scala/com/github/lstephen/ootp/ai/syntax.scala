package com.github.lstephen.ootp.ai

import scala.language.higherKinds

import scalaz.Foldable
import scalaz.syntax.foldable._

import spire.algebra.AdditiveMonoid
import spire.syntax.additiveMonoid._

object syntax {
  implicit class FoldableOfAdditiveMonoid[A: AdditiveMonoid, F[_]: Foldable](xs: F[A]) {
    def msum: A = xs.foldLeft(implicitly[AdditiveMonoid[A]].zero)(_ + _)
  }
}

