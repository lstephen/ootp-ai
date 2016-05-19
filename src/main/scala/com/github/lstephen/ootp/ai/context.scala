package com.github.lstephen.ootp.ai

import com.github.lstephen.ootp.ai.roster.Roster

import scala.Option

object Context {
  var newRoster: Option[Roster] = None
  def newRoster_=(r: Roster): Unit = { newRoster = Some(r) }

  var oldRoster: Option[Roster] = None
  def oldRoster_=(r: Roster): Unit = { oldRoster = Some(r) }
}


