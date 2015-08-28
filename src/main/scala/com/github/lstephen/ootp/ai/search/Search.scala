package com.github.lstephen.ootp.ai.search

import com.github.lstephen.ootp.ai.score._

import scala.collection.GenTraversableOnce


class HillClimbing[S](heuristic: S => Score, searchSpace: S => GenTraversableOnce[S]) {
  def search(s: S): S =
    searchSpace(s) find (heuristic(_) > heuristic(s)) map (search(_)) getOrElse s
}

object BranchAndBound {
  //def search(start: S, h: S => N, n: S => F[S], ub: S => N)
  // start at s, set it as the best, based on h
  // get the next nodes
  // for each next node
  //   if ub(n) < h(best) discard
  //   else put it on the stack
  // loop until stack is empty


}



