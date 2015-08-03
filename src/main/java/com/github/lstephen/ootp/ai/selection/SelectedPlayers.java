package com.github.lstephen.ootp.ai.selection;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.github.lstephen.ootp.ai.player.Player;
import com.github.lstephen.ootp.ai.player.Slot;
import com.github.lstephen.ootp.ai.player.ratings.Position;
import com.github.lstephen.ootp.ai.regression.Predictions;
import com.github.lstephen.ootp.ai.selection.bench.BenchScorer;
import com.github.lstephen.ootp.ai.selection.lineup.AllLineups;
import com.github.lstephen.ootp.ai.selection.lineup.Lineup;
import com.github.lstephen.ootp.ai.selection.lineup.Lineup.VsHand;
import com.github.lstephen.ootp.ai.selection.lineup.LineupSelection;
import com.github.lstephen.ootp.ai.stats.BattingStats;
import com.github.lstephen.ootp.ai.stats.SplitPercentages;
import com.github.lstephen.ootp.ai.stats.TeamStats;

/**
 *
 * @author lstephen
 */
public class SelectedPlayers {

    private final ImmutableSet<Player> players;

    private final Predictions predictions;

    private final SplitPercentages splits;

    private SelectedPlayers(Iterable<Player> players, Predictions predictions, SplitPercentages splits) {
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
        AllLineups lineups = new LineupSelection(predictions.getAllBatting()).select(players);

        return splits.getVsRhpPercentage() * score(VsHand.VS_RHP, lineups.getVsRhpPlusDh())
            + splits.getVsRhpPercentage() * score(VsHand.VS_RHP, lineups.getVsRhp())
            + splits.getVsLhpPercentage() * score(VsHand.VS_LHP, lineups.getVsLhpPlusDh())
            + splits.getVsLhpPercentage() * score(VsHand.VS_LHP, lineups.getVsLhp())
            - players.size()
            - (double) ageScore() / 100000;
    }

    private Double score(Lineup.VsHand vs, Lineup lineup) {
        return hittingScore(vs, lineup) + hittingWithDefenseScore(vs, lineup) + benchScore(vs, lineup);
    }

    private Integer hittingScore(Lineup.VsHand vs, Lineup lineup) {
        Integer score = 0;
        for (Player p : lineup.playerSet()) {
            score += vs.getStats(predictions.getAllBatting(), p).getWobaPlus();
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
                score += vs.getStats(predictions.getAllBatting(), entry.getPlayer()).getWobaPlus();
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
            score += predictions.getAllBatting().getSplits(p).getOverall().getWobaPlus();
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

    public static SelectedPlayers create(Iterable<Player> ps, Predictions predictions, SplitPercentages pcts) {
        return new SelectedPlayers(ps, predictions, pcts);
    }

}
