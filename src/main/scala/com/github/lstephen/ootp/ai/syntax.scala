package com.github.lstephen.ootp.ai

object syntax {
  implicit class MapExtras[A, B](m: Map[A, B]) {
    def swap(x: A, y: A): Map[A, B] = m + (x -> m(y)) + (y -> m(x))
  }

  implicit class FunctionToList[A, B](f: A => List[B]) {
    def ++(g: A => List[B]): A => List[B] = x => f(x) ++ g(x)
    def flatMap[C](g: B => List[C]): A => List[C] = x => f(x) flatMap (g(_))
  }
}

