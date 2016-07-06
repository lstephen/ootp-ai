package com.github.lstephen.ootp.ai.value

import com.github.lstephen.ootp.ai.player.Player
import com.github.lstephen.ootp.ai.player.ratings.Position
import com.github.lstephen.ootp.ai.regression.Predictor
import com.github.lstephen.ootp.ai.selection.lineup.PlayerDefenseScore

import collection.JavaConversions._

trait BatterFutureAbility { this: Ability =>
  override val batting = Some(predictor.predictFutureBatting(player).overall)
  override val defense = Some(new PlayerDefenseScore(player, position).score)
}

trait PitcherFutureAbility { this: Ability =>
  val endurance = position match {
    case Position.MIDDLE_RELIEVER => 0.865
    case Position.STARTING_PITCHER => {
      val end = player.getPitchingRatings.getVsRight.getEndurance;
      (1000.0 - Math.pow(10 - end, 3)) / 1000.0;
    }
  }
  override val pitching = Some(endurance *: predictor.predictFuturePitching(player).overall)
}


object FutureAbility {
  def apply(p: Player, pos: Position)(implicit ps: Predictor): Ability = {
    if (p.getAge < 27) {
      if (p.isHitter && pos.isHitting) {
        return new Ability(p, pos) with BatterFutureAbility
      } else if (p.isPitcher && pos.isPitching) {
        return new Ability(p, pos) with PitcherFutureAbility
      }
    }
    new Ability(p, pos)
  }
}

class FutureValue
  (val player: Player, val position: Position)
  (implicit val predictor: Predictor)
  extends ComponentScore {

  val ability = FutureAbility(player, position)

  val vsReplacement =
   if (player.getAge < 27) {
     val vsCurrent = ReplacementLevels.getForIdeal.get(ability)
     val vsAverage = ReplacementLevels.getForIdeal.getVsAverage(ability);

     Some(List(vsCurrent, vsAverage).average)
   } else
      None

  def components = ability.components :+ vsReplacement

  def format: String = {
    val p = if (score.isPositive) position.getAbbreviation else ""

    components
      .map(_.map(s => f"${s.toLong}%3d"))
      .map(_.getOrElse("   "))
      .mkString(f"${p}%2s : ", " ", f" : ${score.toLong}%3d")
  }
}

object FutureValue {
  def apply(p: Player, pos: Position)(implicit ps: Predictor) =
    new FutureValue(p, pos)

  def apply(p: Player)(implicit ps: Predictor): FutureValue =
    (Position.hitting ++ Position.pitching)
      .map(FutureValue(p, _))
      .max
}
