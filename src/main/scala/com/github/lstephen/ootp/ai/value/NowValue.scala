package com.github.lstephen.ootp.ai.value

import com.github.lstephen.ootp.ai.player.Player
import com.github.lstephen.ootp.ai.player.ratings.Position
import com.github.lstephen.ootp.ai.regression.Predictions
import com.github.lstephen.ootp.ai.score._

import collection.JavaConversions._

class NowValue
  (player: Player, position: Position)
  (implicit predictions: Predictions)
  extends Scoreable {

  val pv = new PlayerValue(predictions, null, null)

  val now = Score(pv.getNowValue(player).intValue)
  val vsReplacement = ReplacementLevels.getForNow.get(player, position);

  val score = now + vsReplacement
}

object NowValue {
  def apply(p: Player)(implicit ps: Predictions): NowValue =
    (Position.hitting ++ Position.pitching)
      .map(new NowValue(p, _))
      .max
}

