package com.ljs.ootp.ai.selection.search;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.ljs.ai.search.State;
import com.ljs.ootp.ai.player.Player;
import com.ljs.ootp.ai.player.Slot;
import com.ljs.ootp.ai.player.ratings.Position;
import com.ljs.ootp.ai.selection.lineup.AllLineups;
import com.ljs.ootp.ai.selection.lineup.Lineup;
import com.ljs.ootp.ai.selection.lineup.Lineup.VsHand;
import com.ljs.ootp.ai.selection.lineup.LineupSelection;
import com.ljs.ootp.ai.stats.BattingStats;
import com.ljs.ootp.ai.stats.SplitPercentages;
import com.ljs.ootp.ai.stats.TeamStats;
import java.util.Set;

/**
 *
 * @author lstephen
 */
public class SelectedPlayers implements State {

    private final ImmutableSet<Player> players;

    public static TeamStats<BattingStats> predictions;

    public static SplitPercentages splits;

    private SelectedPlayers(Iterable<Player> players) {
        this.players = ImmutableSet.copyOf(players);
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

    public SelectedPlayers with(Player p) {
        return new SelectedPlayers(Iterables.concat(players, ImmutableList.of(p)));
    }

    public SelectedPlayers without(Player p) {
        Set<Player> players = Sets.newHashSet(this.players);
        players.remove(p);
        return new SelectedPlayers(players);
    }

    public Boolean isValid() {
        return players.size() >= 9 && players.size() <= 14;
    }

    public Double score() {
        AllLineups lineups = new LineupSelection(predictions).select(players);

        return
            //splits.getVsRhpPercentage() * score(VsHand.VS_RHP, lineups.getVsRhp())
            splits.getVsRhpPercentage() * score(VsHand.VS_RHP, lineups.getVsRhpPlusDh())
            //+ splits.getVsLhpPercentage() * score(VsHand.VS_LHP, lineups.getVsLhp())
            + splits.getVsLhpPercentage() * score(VsHand.VS_LHP, lineups.getVsLhpPlusDh())
            - players.size()
            - (double) ageScore() / 100000;

    }

    private Integer score(Lineup.VsHand vs, Lineup lineup) {
        return hittingScore(vs, lineup) + hittingWithDefenseScore(vs, lineup) + benchScore(vs, lineup);
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

    private Integer benchScore(Lineup.VsHand vs, Lineup lineup) {
        Set<Player> bench = Sets.difference(players, lineup.playerSet());

        Integer score = 0;

        for (Lineup.Entry entry : lineup) {
            Optional<Player> p = selectBenchPlayer(bench, vs, entry.getPositionEnum());

            if (p.isPresent()) {
                score += vs.getStats(predictions, p.get()).getWobaPlus();
            }
        }

        return score;
    }

    private Optional<Player> selectBenchPlayer(Iterable<Player> bench, final Lineup.VsHand vs, Position pos) {
        ImmutableList<Player> sortedBench = Ordering
            .natural()
            .reverse()
            .onResultOf(new Function<Player, Integer>() {
                public Integer apply(Player p) {
                    return vs.getStats(predictions, p).getWobaPlus();
                }
            })
            .compound(Player.byTieBreak())
            .immutableSortedCopy(bench);

        for (Player p : sortedBench) {
            if (p.canPlay(pos)) {
                return Optional.of(p);
            }
        }

        return Optional.absent();
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

    public static SelectedPlayers create(Iterable<Player> ps) {
        return new SelectedPlayers(ps);
    }

}
