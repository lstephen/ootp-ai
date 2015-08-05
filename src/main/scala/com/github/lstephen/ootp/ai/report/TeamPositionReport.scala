package com.github.lstephen.ootp.ai.report

import com.github.lstephen.ootp.ai.io.Printable
import com.github.lstephen.ootp.ai.player.Player
import com.github.lstephen.ootp.ai.player.ratings.Position
import com.github.lstephen.ootp.ai.regression.Predictions
import com.github.lstephen.ootp.ai.roster.Roster
import com.github.lstephen.ootp.ai.selection.lineup.Lineup.VsHand
import com.github.lstephen.ootp.ai.selection.lineup.InLineupScore

import java.io.PrintWriter

import collection.JavaConversions._

class TeamPositionReport(roster: Roster)(implicit predictions: Predictions) extends Printable {

  override def print(pw: PrintWriter): Unit = {
    implicit val w = pw
    Position.hitting.foreach { print(_) }
  }

  def print(pos: Position)(implicit w: PrintWriter): Unit = {
    w.println()
    w.println(s"$pos")

    val ovr = top(pos)
    val vsL = top(pos, Some(VsHand.VS_LHP))
    val vsR = top(pos, Some(VsHand.VS_RHP))

    (ovr, vsL, vsR)
      .zipped
      .toSeq
      .take(10)
      .zipWithIndex
      .foreach { case ((o, l, r), idx) =>
        w.println(f"${idx + 1}%2d | ${format(o)} | ${format(l)} | ${format(r)}")
      }
  }

  def format(s: InLineupScore): String = {
    f"${s.name}%-16s ${s.hitting}%3d ${s.defense}%3.0f ${s.total}%3.0f"
  }

  def top(p: Position, vs: Option[VsHand] = None): List[InLineupScore] = {
    (List() ++ roster.getAllPlayers)
      .map(new InLineupScore(_, p, vs))
      .sortBy(_.total)
      .reverse
  }

}
