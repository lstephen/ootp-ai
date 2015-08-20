package com.github.lstephen.ootp.ai

import spire.algebra._
import spire.implicits._

object algebra {
  implicit class MonoidSeq[A: Monoid](xs: Seq[A]) {
    def mconcat: A = xs.foldLeft(implicitly[Monoid[A]].id)(_ |+| _)
  }
}

