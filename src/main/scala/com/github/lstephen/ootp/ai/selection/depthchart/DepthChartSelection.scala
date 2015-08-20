package com.github.lstephen.ootp.ai.selection.depthchart

import com.github.lstephen.ootp.ai.player.Player
import com.github.lstephen.ootp.ai.player.ratings.Position
import com.github.lstephen.ootp.ai.regression.Predictions
import com.github.lstephen.ootp.ai.selection.bench.BenchScorer
import com.github.lstephen.ootp.ai.selection.lineup.All
import com.github.lstephen.ootp.ai.selection.lineup.AllLineups
import com.github.lstephen.ootp.ai.selection.lineup.Defense
import com.github.lstephen.ootp.ai.selection.lineup.InLineupScore
import com.github.lstephen.ootp.ai.selection.lineup.Lineup
import com.github.lstephen.ootp.ai.selection.lineup.Lineup.VsHand

import com.github.lstephen.ai.search.HillClimbing
import com.github.lstephen.ai.search.Heuristic
import com.github.lstephen.ai.search.Validator
import com.github.lstephen.ai.search.action.Action
import com.github.lstephen.ai.search.action.ActionGenerator
import com.github.lstephen.ai.search.action.SequencedAction

import collection.JavaConversions._

class DepthChartSelection(implicit predictions: Predictions) {

  def select(lineups: AllLineups, available: java.lang.Iterable[Player]): AllDepthCharts =
    select(lineups, Set() ++ available)

  def select(lineups: AllLineups, available: Set[Player]): AllDepthCharts = {
    AllDepthCharts.create(All
      .builder()
      .vsRhp(select(lineups.getVsRhp(), available, VsHand.VS_RHP))
      .vsRhpPlusDh(select(lineups.getVsRhpPlusDh(), available, VsHand.VS_RHP))
      .vsLhp(select(lineups.getVsLhp(), available, VsHand.VS_LHP))
      .vsLhpPlusDh(select(lineups.getVsLhpPlusDh(), available, VsHand.VS_LHP))
      .build());
  }

  def select(lineup: Lineup, available: Set[Player], vs: VsHand): DepthChart = {
    val dc = new DepthChart

    val bench = available -- lineup.playerSet

    for (entry <- lineup if entry.getPositionEnum != Position.PITCHER) {
      dc.setStarter(entry.getPositionEnum, entry.getPlayer)

      if (entry.getPositionEnum != Position.DESIGNATED_HITTER) {
        selectDefensiveReplacement(entry.getPositionEnum, entry.getPlayer, bench)
          .foreach(dc.setDefensiveReplacement(entry.getPositionEnum, _))
      }

      addBackups(dc, entry.getPositionEnum(), bench, vs)
    }

    dc
  }

  def selectDefensiveReplacement(position: Position, starter: Player, bench: Set[Player]): Option[Player] = {
    if (bench.isEmpty) return None

    val candidate = bench
      .toSeq
      .sortBy(_.getDefensiveRatings.getPositionScore(position))
      .reverse
      .head

    def defense(p: Player): Double = p.getDefensiveRatings.getPositionScore(position)

    if (defense(candidate) > defense(starter) * 1.1) Some(candidate) else None
  }

  def addBackups(dc: DepthChart, position: Position, bench: Set[Player], vs: VsHand): Unit = {
    val starter = dc getStarter position
    val backups = InLineupScore.sort(bench, position, vs).take(3)

    new BackupPercentageSelection(starter, backups, position, vs)
      .select
      .zip(backups)
      .filter { case (pct, _) => pct > 0 }
      .foreach { case (pct, ply) => dc.addBackup(position, ply, pct) }
  }
}


class BackupPercentageSelection
  (starter: Player, backups: Seq[Player], position: Position, vs: VsHand)
  (implicit predictions: Predictions) {

  type BackupPercentage = Array[Int]

  def select: BackupPercentage = {
    HillClimbing
      .builder[BackupPercentage]
      .validator(validator)
      .heuristic(heuristic)
      .actionGenerator(actionGenerator)
      .build
      .search(Array(1) ++ Array.fill(backups.size - 1)(0))
  }

  val validator: Validator[BackupPercentage] = new Validator[BackupPercentage] {
    override def test(pcts: BackupPercentage): Boolean = {
      pcts.size == backups.size &&
        pcts.forall(_ >= 0) &&
        pcts.sum < 100 &&
        pcts.sliding(2).forall(pair => pair(0) >= pair(1))
    }
  }

  val heuristic: Heuristic[BackupPercentage] = new Heuristic[BackupPercentage] {
    override def compare(lhs: BackupPercentage, rhs: BackupPercentage) =
      score(lhs) compare score(rhs)

    def score(pcts: BackupPercentage): Double = {
      val s = new WithPlayingTimeScore(starter, 100 - pcts.sum, position, vs).total
      val b = (backups zip pcts)
        .map { case (ply, pct) => new WithPlayingTimeScore(ply, pct, position, vs).total }
        .sum

      s + b
    }
  }

  def actionGenerator: ActionGenerator[BackupPercentage] = new ActionGenerator[BackupPercentage] {
    override def apply(pcts: BackupPercentage): java.util.stream.Stream[Action[BackupPercentage]] =
      toJavaActionList(actions(pcts.size)).stream

    def actions(size: Int) : Seq[BackupPercentage => BackupPercentage] = {
      def increase(i: Int): BackupPercentage => BackupPercentage =
        p => if (p(i) == 1) p.updated(i, 5) else p.updated(i, p(i) + 5)

      def decrease(i: Int): BackupPercentage => BackupPercentage = p => p.updated(i, p(i) - 5)

      0 until size flatMap (s => List(increase(s), decrease(s)))
    }

    def toAction[S](f: S => S): Action[S] = new Action[S] {
      override def apply(s: S) = f(s)
    }

    def toJavaActionList[S](fs: Seq[S => S]): java.util.List[Action[S]] =
      seqAsJavaList(fs map (toAction(_)))
  }
}

class WithPlayingTimeScore
  (player: Player, percentage: Integer, position: Position, vs: VsHand)
  (implicit predictions: Predictions) {

  val base = InLineupScore(player, position, vs).total.toDouble * percentage
  val fatigue = (Defense.getPositionFactor(position) * percentage * percentage / 10.0) / 2.0

  // Simplies to (s/100)p + (f/2000)p^2
  val total = (base - fatigue) / 100.0
}

