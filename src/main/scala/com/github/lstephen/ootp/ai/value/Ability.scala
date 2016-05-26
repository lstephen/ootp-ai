package com.github.lstephen.ootp.ai.value

import com.github.lstephen.ootp.ai.player.Player
import com.github.lstephen.ootp.ai.player.ratings.Position
import com.github.lstephen.ootp.ai.regression.Predictor
import com.github.lstephen.ootp.ai.score._

trait ComponentScore extends Scoreable {
  def components: List[Option[Score]]
  def score: Score = components.total //map(_.getOrElse(Score.zero)).total
}

class Ability
  (val player: Player, val position: Position)
  (implicit val predictor: Predictor)
  extends ComponentScore {

  val batting: Option[Score] = None
  val pitching: Option[Score] = None
  val defense: Option[Score] = None

  def components = List(batting, pitching, defense)
}

