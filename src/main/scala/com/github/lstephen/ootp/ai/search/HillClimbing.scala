package com.github.lstephen.ootp.ai.search

import com.github.lstephen.ootp.ai.score._

import scala.collection.GenTraversableOnce

class HillClimbing[S](heuristic: S => Score,
                      searchSpace: S => GenTraversableOnce[S]) {
  def search(s: S): S =
    searchSpace(s) find (heuristic(_) > heuristic(s)) map (search(_)) getOrElse s
}
