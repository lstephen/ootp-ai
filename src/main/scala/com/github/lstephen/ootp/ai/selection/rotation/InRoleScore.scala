package com.github.lstephen.ootp.ai.selection.rotation

import com.github.lstephen.ootp.ai.player.Clutch._
import com.github.lstephen.ootp.ai.player.Consistency._
import com.github.lstephen.ootp.ai.player.Player
import com.github.lstephen.ootp.ai.regression.Predictions
import com.github.lstephen.ootp.ai.score._

import scala.math._


sealed trait Role {
  def endurance(s: InRoleScore): Score = Score.zero
  def consistency(s: InRoleScore): Score = Score.zero
  def clutch(s: InRoleScore): Score = Score.zero
}

trait WithClutch extends Role {
  override def clutch(s: InRoleScore): Score = s.clutchRating match {
      case SUFFERS => - 0.05 *: s.pitching
      case NORMAL  => Score.zero
      case GREAT   => 0.05 *: s.pitching
    }
}

trait WithConsistency extends Role {
  override def consistency(s: InRoleScore): Score = s.consistencyRating match {
      case VERY_INCONSISTENT => - 0.05 *: s.pitching
      case AVERAGE  => Score.zero
      case GOOD   => 0.05 *: s.pitching
    }
}

trait IsReliever extends Role {
  override def endurance(s: InRoleScore): Score = - (1.0 - 0.865) *: s.pitching
}

case object SP extends Role {
  override def endurance(s: InRoleScore): Score =
    - (1.0 - (1000.0 - pow(10 - s.enduranceRating, 3)) / 1000) *: s.pitching
}

case object MR extends Role with IsReliever
case object SU extends Role with IsReliever with WithConsistency
case object CL extends Role with IsReliever with WithClutch with WithConsistency

object Role {
  val all: List[Role] = List(SP, MR, SU, CL)
}

class InRoleScore(player: Player, role: Role)(implicit ps: Predictions) extends Scoreable {
  val name = player.getShortName

  val pitching = Score(ps.getOverallPitching(player))

  val clutchRating = player.getClutch.or(NORMAL)
  val consistencyRating = player.getConsistency.or(AVERAGE)
  val enduranceRating = player.getPitchingRatings.getVsRight.getEndurance

  val endurance = role endurance this
  val consistency = role consistency this
  val clutch = role clutch this

  val inRole = endurance + consistency + clutch

  val score = pitching + inRole
}

object InRoleScore {
  def best(ply: Player)(implicit ps: Predictions): InRoleScore =
    Role.all
      .map(new InRoleScore(ply, _))
      .max
}



