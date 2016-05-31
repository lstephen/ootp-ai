package com.github.lstephen.ootp.ai.roster

import collection.JavaConversions._

class RosterBalance(r: Roster) {

  val hitterCount: Double = r.getAllPlayers.filter(_.isHitter).size
  val pitcherCount: Double = r.getAllPlayers.filter(_.isPitcher).size

  val ideal = 14.0 / 11.0 // ML hitter count / ML pitcher count
  val actual = hitterCount / pitcherCount

  val isHitterHeavy = actual > ideal
  val isPitcherHeavy = actual < ideal

  val format: String =
    f"${hitterCount}%.0f/${pitcherCount.toInt}%.0f = ${actual}%.3f (ideal: ${ideal}%.3f)"
}

