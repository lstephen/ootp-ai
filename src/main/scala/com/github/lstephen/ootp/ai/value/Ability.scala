package com.github.lstephen.ootp.ai.value

import com.github.lstephen.ootp.ai.player.Player
import com.github.lstephen.ootp.ai.player.ratings.Position
import com.github.lstephen.ootp.ai.regression.Predictor
import com.github.lstephen.ootp.ai.score._

trait ComponentScore extends Scoreable {
  def components: List[Option[Score]]
  def score: Score = components.total //map(_.getOrElse(Score.zero)).total
}

class Ability(val player: Player, val position: Position)(
    implicit val predictor: Predictor)
    extends ComponentScore {

  val batting: Option[Score] = None
  val pitching: Option[Score] = None
  val defense: Option[Score] = None

  def components = List(batting, pitching, defense)

  def format: String =
    components
      .map(_.map(s => f"${s.toLong}%3d"))
      .map(_.getOrElse("   "))
      .mkString(f"${position.getAbbreviation}%2s : ",
                " ",
                f" : ${score.toLong}%3d")
}
