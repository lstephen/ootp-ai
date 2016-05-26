package com.github.lstephen.ootp.ai.value

import com.github.lstephen.ootp.ai.player.Player
import com.github.lstephen.ootp.ai.regression.Predictor
import com.github.lstephen.ootp.ai.score._

class OverallValue
  (val player: Player)
  (implicit val predictor: Predictor)
  {

  val now = NowValue(player).score
  val future = FutureValue(player).score

  def age = {
    val base = 27 - player.getAge

    val oldAgePenalty = if (player.getAge <= 33) 0 else Math.pow(player.getAge - 33, 2)

    Score(base - oldAgePenalty)
  }

  def score = Score.max(now, future) + age

  def format: String =
    f"${age.toLong}%3d | ${score.toLong}%3d"
}

object OverallValue {
  def apply(p: Player)(implicit predictor: Predictor) = new OverallValue(p)
}
