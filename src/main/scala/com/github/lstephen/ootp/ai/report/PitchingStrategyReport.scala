package com.github.lstephen.ootp.ai.report

import com.github.lstephen.ootp.ai.io.Printable
import com.github.lstephen.ootp.ai.player.ratings.Position
import com.github.lstephen.ootp.ai.regression.Predictor
import com.github.lstephen.ootp.ai.selection.rotation.Rotation
import com.github.lstephen.ootp.ai.selection.rotation.Rotation.Role
import com.github.lstephen.ootp.ai.score.Score
import com.github.lstephen.ootp.ai.value.NowAbility

import java.io.PrintWriter;

import collection.JavaConversions._

class PitchingStrategyReport(rotation: Rotation)(implicit predictor: Predictor)
    extends Printable {
  val chainValues = List(1.8, 1.3, 1.0, 0.9)

  val bullpenNowAbility =
    (chainValues, rotation.get(Role.CL, Role.SU, Role.MR)).zipped
      .map(_ *: predictor.predictPitching(_).overall)
      .average

  val bullpenEndurance =
    rotation.get(Role.LR).map(_.getPitchingRatings.getVsRight.getEndurance * 1.0).sum +
    rotation.get(Role.MR).map(_.getPitchingRatings.getVsRight.getEndurance * 0.5).sum

  def print(w: PrintWriter): Unit = {
    w.println

    w println f"BP Ability: ${bullpenNowAbility.toLong}%d"
    w println f"BP Endrnce: ${bullpenEndurance}%s"

    w.println

    w println "--- Slow Hook ---"
    rotation
      .get(Role.SP)
      .filter(
        NowAbility(_, Position.STARTING_PITCHER).score > bullpenNowAbility)
      .foreach(w println _.getName)
  }
}
