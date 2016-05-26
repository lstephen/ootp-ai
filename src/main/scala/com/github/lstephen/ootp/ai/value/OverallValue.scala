package com.github.lstephen.ootp.ai.value

import com.github.lstephen.ootp.ai.player.Player
import com.github.lstephen.ootp.ai.player.ratings.Position
import com.github.lstephen.ootp.ai.regression.Predictor
import com.github.lstephen.ootp.ai.score._

class OverallValue
  (val player: Player, position: Option[Position] = None)
  (implicit val predictor: Predictor)
  {

  val now = position match {
    case None => NowValue(player).score
    case Some(p) => NowValue(player, p).score
  }

  val future = position match {
    case None => FutureValue(player).score
    case Some(p) => FutureValue(player, p).score
  }

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
  def apply(p: Player, pos: Position)(implicit predictor: Predictor) = new OverallValue(p, Some(pos))
}
