package com.github.lstephen.ootp.ai

import com.github.lstephen.ootp.ai.player.Player
import com.github.lstephen.ootp.ai.roster.Roster

import collection.JavaConversions._

object Context {
  var newRoster: Option[Roster] = None
  def newRoster_=(r: Roster): Unit = { newRoster = Some(r) }

  var oldRoster: Option[Roster] = None
  def oldRoster_=(r: Roster): Unit = { oldRoster = Some(r) }

  var idealRoster: Option[Roster] = None
  def idealRoster_=(r: Roster): Unit = { idealRoster = Some(r) }
}


