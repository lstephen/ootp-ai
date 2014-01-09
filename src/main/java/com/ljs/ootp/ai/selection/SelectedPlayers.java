package com.ljs.ootp.ai.selection;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.ljs.ootp.ai.player.Player;
import com.ljs.ootp.ai.player.Slot;
import com.ljs.ootp.ai.player.ratings.Position;
import com.ljs.ootp.ai.selection.bench.BenchScorer;
import com.ljs.ootp.ai.selection.lineup.AllLineups;
import com.ljs.ootp.ai.selection.lineup.Lineup;
import com.ljs.ootp.ai.selection.lineup.Lineup.VsHand;
import com.ljs.ootp.ai.selection.lineup.LineupSelection;
import com.ljs.ootp.ai.stats.BattingStats;
import com.ljs.ootp.ai.stats.SplitPercentages;
import com.ljs.ootp.ai.stats.TeamStats;

/**
 *
 * @author lstephen
 */
public class SelectedPlayers {

    private final ImmutableSet<Player> players;

    private final TeamStats<BattingStats> predictions;

    private final SplitPercentages splits;

    private SelectedPlayers(Iterable<Player> players, TeamStats<BattingStats> predictions, SplitPercentages splits) {
        this.players = ImmutableSet.copyOf(players);
        this.predictions = predictions;
        this.splits = splits;
    }

    public ImmutableMultimap<Slot, Player> selection() {
        Multimap<Slot, Player> sel = HashMultimap.create();

        for (Player p : players) {
            sel.put(Slot.getPrimarySlot(p), p);
        }

        return ImmutableMultimap.copyOf(sel);
    }

    public ImmutableSet<Player> players() {
        return players;
    }

    public Boolean contains(Player p) {
        return players.contains(p);
    }

    public Double score() {
        AllLineups lineups = new LineupSelection(predictions).select(players);

        return
            splits.getVsRhpPercentage() * score(VsHand.VS_RHP, lineups.getVsRhp())
            + splits.getVsRhpPercentage() * score(VsHand.VS_RHP, lineups.getVsRhpPlusDh())
            + splits.getVsLhpPercentage() * score(VsHand.VS_LHP, lineups.getVsLhp())
            + splits.getVsLhpPercentage() * score(VsHand.VS_LHP, lineups.getVsLhpPlusDh())
            - players.size()
            - (double) ageScore() / 100000;

    }

    private Double score(Lineup.VsHand vs, Lineup lineup) {
        return 2 * hittingScore(vs, lineup) + hittingWithDefenseScore(vs, lineup) + benchScore(vs, lineup);
    }

    private Integer hittingScore(Lineup.VsHand vs, Lineup lineup) {
        Integer score = 0;
        for (Player p : lineup.playerSet()) {
            score += vs.getStats(predictions, p).getWobaPlus();
        }
        return score;
    }

    private Integer hittingWithDefenseScore(Lineup.VsHand vs, Lineup lineup) {
        Integer score = 0;
        for (Lineup.Entry entry : lineup) {
            Position pos = entry.getPositionEnum();
            if (pos == Position.PITCHER) {
                continue;
            }
            if (entry.getPlayer().canPlay(entry.getPositionEnum())) {
                score += vs.getStats(predictions, entry.getPlayer()).getWobaPlus();
            }
        }
        return score;
    }

    private Double benchScore(Lineup.VsHand vs, Lineup lineup) {
        return new BenchScorer(predictions).score(Sets.difference(players, lineup.playerSet()), lineup, vs);
    }

    private Integer overallHittingScore() {
        Integer score = 0;
        for (Player p : players) {
            // This already combines according to split percentages
            score += predictions.getSplits(p).getOverall().getWobaPlus();
        }
        return score;
    }

    private Integer ageScore() {
        Integer score = 0;
        for (Player p : players) {
            score += p.getAge();
        }
        return score;
    }

    public static SelectedPlayers create(Iterable<Player> ps, TeamStats<BattingStats> predictions, SplitPercentages pcts) {
        return new SelectedPlayers(ps, predictions, pcts);
    }

}
