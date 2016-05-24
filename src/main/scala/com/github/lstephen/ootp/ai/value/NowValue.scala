package com.github.lstephen.ootp.ai.value

import com.github.lstephen.ootp.ai.player.Player
import com.github.lstephen.ootp.ai.player.ratings.Position
import com.github.lstephen.ootp.ai.regression.Predictor
import com.github.lstephen.ootp.ai.score._
import com.github.lstephen.ootp.ai.selection.lineup.PlayerDefenseScore

import collection.JavaConversions._

trait ComponentScore extends Scoreable {

  def components: List[Option[Score]]

  def score: Score = components.map(_.getOrElse(Score.zero)).total
}

class NowAbility
  (val player: Player, val position: Position)
  (implicit val predictor: Predictor)
  extends ComponentScore {

  val batting: Option[Score] = None
  val pitching: Option[Score] = None
  val defense: Option[Score] = None

  def components = List(batting, pitching, defense)
}

trait BatterNowAbility { this: NowAbility =>
  override val batting = Some(predictor.predictBatting(player).overall)
  override val defense = Some(new PlayerDefenseScore(player, position).score)
}

trait PitcherNowAbility { this: NowAbility =>
  val endurance = position match {
    case Position.MIDDLE_RELIEVER => 0.865
    case Position.STARTING_PITCHER => {
      val end = player.getPitchingRatings.getVsRight.getEndurance;
      (1000.0 - Math.pow(10 - end, 3)) / 1000.0;
    }
  }
  override val pitching = Some(endurance *: predictor.predictPitching(player).overall)
}

object NowAbility {
  def apply(p: Player, pos: Position)(implicit ps: Predictor): NowAbility = {
    if (p.isHitter && pos.isHitting) {
      return new NowAbility(p, pos) with BatterNowAbility
    } else if (p.isPitcher && pos.isPitching) {
      return new NowAbility(p, pos) with PitcherNowAbility
    }
    new NowAbility(p, pos)
  }
}


class NowValue
  (val player: Player, val position: Position)
  (implicit val predictor: Predictor)
  extends ComponentScore {

  val ability = NowAbility(player, position)

  val vsReplacement = Some(ReplacementLevels.getForIdeal.get(player, position))

  def components = ability.components :+ vsReplacement

  def format: String =
    components
      .map(_.map(s => f"${s.toLong}%3d"))
      .map(_.getOrElse("   "))
      .mkString(f"${position.getAbbreviation}%2s : ", " ", f" : ${score.toLong}%3d")
}

object NowValue {
  def apply(p: Player, pos: Position)(implicit ps: Predictor) =
    new NowValue(p, pos)

  def apply(p: Player)(implicit ps: Predictor): NowValue =
    (Position.hitting ++ Position.pitching)
      .map(NowValue(p, _))
      .max
}

