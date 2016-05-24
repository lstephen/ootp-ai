package com.github.lstephen.ootp.ai.value

import com.github.lstephen.ootp.ai.player.Player
import com.github.lstephen.ootp.ai.player.ratings.Position
import com.github.lstephen.ootp.ai.regression.Predictions
import com.github.lstephen.ootp.ai.score._
import com.github.lstephen.ootp.ai.selection.lineup.PlayerDefenseScore

import collection.JavaConversions._

abstract class NowValue
  (val player: Player, val position: Position)
  (implicit val predictions: Predictions)
  extends Scoreable {

  val batting: Option[Score] = None
  val pitching: Option[Score] = None
  val defense: Option[Score] = None

  val vsReplacement = Some(ReplacementLevels.getForNow.get(player, position))

  def components = List(batting, pitching, defense, vsReplacement)

  def score: Score = components.map(_.getOrElse(Score.zero)).total

  def format: String =
    components
      .map(_.map(s => f"${s.toLong}%3d"))
      .map(_.getOrElse("   "))
      .mkString(f"${position.getAbbreviation}%2s : ", " ", f" : ${score.toLong}%3d")
}

trait BatterNowValue { this: NowValue =>
  override val batting = Some(Score(predictions.getOverallHitting(player).intValue))
  override val defense = Some(new PlayerDefenseScore(player, position).score)
}

trait PitcherNowValue { this: NowValue =>
  override val pitching = Some(Score(predictions.getOverallPitching(player).intValue))
}


object NowValue {
  def apply(p: Player, pos: Position)(implicit ps: Predictions): NowValue = {
    if (p.isHitter()) {
      return new NowValue(p, pos) with BatterNowValue
    } else if (p.isPitcher()) {
      return new NowValue(p, pos) with PitcherNowValue
    }
    throw new IllegalStateException
  }

  def apply(p: Player)(implicit ps: Predictions): NowValue =
    (Position.hitting ++ Position.pitching)
      .map(NowValue(p, _))
      .max
}

