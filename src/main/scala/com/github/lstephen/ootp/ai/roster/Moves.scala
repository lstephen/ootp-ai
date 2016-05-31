package com.github.lstephen.ootp.ai.roster

import com.github.lstephen.ootp.ai.player.Player
import com.github.lstephen.ootp.ai.regression.Predictor
import com.github.lstephen.ootp.ai.score._
import com.github.lstephen.ootp.ai.site.SiteHolder
import com.github.lstephen.ootp.ai.value.NowValue
import com.github.lstephen.ootp.ai.value.OverallValue

import scala.collection.JavaConverters._
import collection.JavaConversions._

class Moves(roster: Roster)(implicit predictor: Predictor) {

  val sign: List[Player] = {
    SiteHolder
      .get
      .getFreeAgents
      .toList
      .filter(if (roster.isLarge) NowValue(_).vsReplacement.orElseZero > Score.zero else _ => true)
      .filter(if (roster.isPitcherHeavy) _.isPitcher else _.isHitter)
      .sortBy(OverallValue(_))
      .reverse
      .take(1)
  }



  def release: List[Player] = {
    if (roster.isSmall) return List()

    roster
      .getAllPlayers
      .toList
      .filter(if (roster.isPitcherHeavy) _.isPitcher else _.isHitter)
      .sortBy(OverallValue(_))
      .take(1)
  }

  def getSign: java.util.List[Player] = sign.asJava
  def getRelease: java.util.List[Player] = release.asJava

}

