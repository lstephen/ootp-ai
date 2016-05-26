package com.github.lstephen.ootp.ai.regression

import com.github.lstephen.ootp.ai.player.Player
import com.github.lstephen.ootp.ai.score._
import com.github.lstephen.ootp.ai.stats.BattingStats
import com.github.lstephen.ootp.ai.stats.PitcherOverall
import com.github.lstephen.ootp.ai.stats.PitchingStats
import com.github.lstephen.ootp.ai.stats.SplitStats

class Predictor(br: BattingRegression, pr: PitchingRegression, po: PitcherOverall) {
  def predictBatting(p: Player): BattingPrediction =
    new BattingPrediction(br.predict(p))

  def predictFutureBatting(p: Player): BattingPrediction =
    new BattingPrediction(br.predictFuture(p))

  def predictPitching(p: Player): PitchingPrediction =
    new PitchingPrediction(pr.predict(p), po)

  def predictFuturePitching(p: Player): PitchingPrediction =
    new PitchingPrediction(pr.predictFuture(p), po)
}

class BattingPrediction(stats: SplitStats[BattingStats]) {
  val overall = Score(stats.getOverall.getWobaPlus)
}


class PitchingPrediction(stats: SplitStats[PitchingStats], po: PitcherOverall) {
  val overall = Score(po.getPlus(stats.getOverall))
}
