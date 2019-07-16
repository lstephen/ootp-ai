package com.github.lstephen.ootp.ai.roster

import com.github.lstephen.ootp.ai.player.Player
import com.github.lstephen.ootp.ai.regression.Predictor
import com.github.lstephen.ootp.ai.score._
import com.github.lstephen.ootp.ai.site.SiteHolder
import com.github.lstephen.ootp.ai.value.NowValue
import com.github.lstephen.ootp.ai.value.OverallValue

import scala.collection.JavaConverters._
import collection.JavaConversions._

class Moves(roster: Roster, changes: Changes)(implicit predictor: Predictor) {

  val sign: List[Player] = {
    val fas = SiteHolder.get.getFreeAgents.toList

    val nows = fas.filter(NowValue(_).vsReplacement.orElseZero > Score.zero)
    val alls =
      fas.filter(if (roster.isPitcherHeavy) _.isHitter else _.isPitcher)

    val dontAcquire = changes get Changes.ChangeType.DONT_ACQUIRE

    (if (roster.isLarge) nows else nows ++ alls).distinct
      .filter(!dontAcquire.contains(_))
      .sortBy(OverallValue(_))
      .reverse
      .take(1)
  }

  def release: List[Player] = {
    if (roster.isSmall) return List()

    roster.getAllPlayers.toList
      .filter(if (roster.isPitcherHeavy) _.isPitcher else _.isHitter)
      .filter(roster.getStatus(_) != Roster.Status.ML)
      .sortBy(OverallValue(_))
      .take(if (roster.isExtraLarge) 5 else 1)
  }

  def getSign: java.util.List[Player] = sign.asJava
  def getRelease: java.util.List[Player] = release.asJava

}
