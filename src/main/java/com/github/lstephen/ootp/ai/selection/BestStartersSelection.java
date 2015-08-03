package com.github.lstephen.ootp.ai.selection;

import com.github.lstephen.ootp.ai.player.Player;
import com.github.lstephen.ootp.ai.player.Slot;
import com.github.lstephen.ootp.ai.player.ratings.Position;
import com.github.lstephen.ootp.ai.regression.Predictions;
import com.github.lstephen.ootp.ai.selection.bench.Bench;
import com.github.lstephen.ootp.ai.selection.lineup.AllLineups;
import com.github.lstephen.ootp.ai.selection.lineup.Defense;
import com.github.lstephen.ootp.ai.selection.lineup.Lineup;
import com.github.lstephen.ootp.ai.selection.lineup.LineupSelection;
import com.github.lstephen.ootp.ai.selection.lineup.StarterSelection;
import com.github.lstephen.ootp.ai.stats.BattingStats;
import com.github.lstephen.ootp.ai.stats.SplitPercentages;
import com.github.lstephen.ootp.ai.stats.TeamStats;

import java.util.Set;

import com.google.common.base.Function;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multiset;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;

/**
 *
 * @author levi.stephen
 */
public class BestStartersSelection implements Selection {

    private static SplitPercentages pcts;

	private final Multiset<Slot> slots;

	private final Predictions predictions;

	private final Function<Player, Integer> value;

	public BestStartersSelection(Iterable<Slot> slots, Predictions predictions, Function<Player, Integer> value) {
		this.slots = HashMultiset.create(slots);
		this.predictions = predictions;
		this.value = value;
	}

    public static void setPercentages(SplitPercentages pcts) {
        BestStartersSelection.pcts = pcts;
    }

    private Integer getTargetSize() {
        return slots.size();
    }

	@Override
	public ImmutableMultimap<Slot, Player> select(Iterable<Player> forced, Iterable<Player> available) {

        ImmutableSet<Player> best = ImmutableSet.copyOf(Iterables.concat(selectStarters(available), forced));

        System.out.println("Best:");
        for (Player p : Player.byShortName().sortedCopy(best)) {
            System.out.print(p.getShortName() + "/");
        }
        System.out.println();


        best = optimize(best, forced, available);

        Multimap<Slot, Player> result = HashMultimap.create();

        for (Player p : best) {
            result.put(Slot.getPrimarySlot(p), p);
        }

        return ImmutableMultimap.copyOf(result);
	}

    private ImmutableSet<Player> optimize(Iterable<Player> best, Iterable<Player> forced, Iterable<Player> available) {
        return optimize(ImmutableSet.copyOf(best), ImmutableSet.copyOf(forced), ImmutableSet.copyOf(available));

    }

    private ImmutableSet<Player> optimize(ImmutableSet<Player> best, ImmutableSet<Player> forced, ImmutableSet<Player> available) {

        if (forced.size() >= getTargetSize()) {
            return forced;
        }

        if (best.size() > getTargetSize()) {
            return optimize(limit(best, forced, getTargetSize()), forced, available);
        }

        Double bestScore = 0.0;
        ImmutableSet<Player> bestPlayers = null;

        Integer limit = best.size();

        while (limit >= 9) {
            ImmutableSet<Player> ps = limit(best, forced, limit);

            ps = fill(ps, available);
            System.out.print("Filled: ");
            for (Player p : Player.byShortName().sortedCopy(ps)) {
                System.out.print(p.getShortName() + "/");
            }
            System.out.println();

            Double score = SelectedPlayers.create(ps, predictions, pcts).score();

            System.out.println("limit:" + limit + " score:" + score);

            if (score < bestScore || ps.equals(bestPlayers)) {
                return bestPlayers;
            }

            bestScore = score;
            bestPlayers = ps;

            limit--;
        }

        return bestPlayers;
    }

    private ImmutableSet<Player> fill(ImmutableSet<Player> partial, ImmutableSet<Player> available) {
        if (partial.size() == getTargetSize()) {
            return partial;
        }

        AllLineups lineups = new LineupSelection(predictions.getAllBatting()).dontRequireBackupCatcher().select(partial);

        Bench bench = Bench.select(lineups, partial, predictions, available, getTargetSize());

        return ImmutableSet.copyOf(Iterables.concat(partial, bench.players()));
    }

    private ImmutableSet<Player> limit(ImmutableSet<Player> best, ImmutableSet<Player> forced, Integer size) {

        Set<Player> selected = Sets.newHashSet(best);

        while (selected.size() > size) {
            AllLineups lineups = new LineupSelection(predictions.getAllBatting()).dontRequireBackupCatcher().select(selected);
            Set<Player> ps = Sets.newHashSet(lineups.getAllPlayers());

            Iterables.removeAll(ps, forced);

            if (ps.isEmpty()) {
                break;
            }

            System.out.print("Selected:");
            for (Player p : byValueProvided(lineups, Iterables.concat(forced, ps)).reverse().sortedCopy(selected)) {
                System.out.print(p.getShortName() + "-" + Math.round(getValueProvided(p, lineups, Iterables.concat(forced, ps))) + "/");
            }
            System.out.println();

            selected.remove(byValueProvided(lineups, selected).min(ps));
        }

        AllLineups lineups = new LineupSelection(predictions.getAllBatting()).dontRequireBackupCatcher().select(selected);

        System.out.print("Limited:");
        for (Player p : byValueProvided(lineups, selected).reverse().sortedCopy(selected)) {
            System.out.print(p.getShortName() + "-" + Math.round(getValueProvided(p, lineups, selected)) + "/");
        }
        System.out.println();

        return ImmutableSet.copyOf(selected);
    }

    private ImmutableSet<Player> selectStarters(Iterable<Player> ps) {
		StarterSelection starters = new StarterSelection(predictions.getAllBatting());

        starters.dontRequireBackupCatcher();

        return ImmutableSet.copyOf(
            Iterables.concat(
                starters.selectWithDh(Lineup.VsHand.VS_LHP, ps),
                starters.selectWithDh(Lineup.VsHand.VS_RHP, ps)));
    }

	private Ordering<Player> byOverall() {
		return Ordering
			.natural()
			.reverse()
			.onResultOf(value)
			.compound(Player.byTieBreak());
	}

    private Ordering<Player> byValueProvided(final AllLineups lineups, final Iterable<Player> selected) {
        return Ordering
            .natural()
            .onResultOf(new Function<Player, Double>() {
                public Double apply(Player p) {
                    return getValueProvided(p, lineups, selected);
                }
            })
            .compound(Player.byTieBreak());
    }

    private Double getValueProvided(Player p, AllLineups lineups, Iterable<Player> selected) {
        Double score = 0.0;

        score += getValueProvided(p, lineups.getVsLhpPlusDh(), selected, pcts.getVsLhpPercentage(), Lineup.VsHand.VS_LHP);
        score += getValueProvided(p, lineups.getVsLhp(), selected, pcts.getVsLhpPercentage(), Lineup.VsHand.VS_LHP);

        score += getValueProvided(p, lineups.getVsRhpPlusDh(), selected, pcts.getVsRhpPercentage(), Lineup.VsHand.VS_RHP);
        score += getValueProvided(p, lineups.getVsRhp(), selected, pcts.getVsRhpPercentage(), Lineup.VsHand.VS_RHP);

        return score;
    }

    private Double getValueProvided(Player p, Lineup l, Iterable<Player> selected, Double pct, Lineup.VsHand vs) {
        Double score = 0.0;
        Integer wobaPlus = predictions.getHitting(p, vs);

        if (l.contains(p)) {
            score += wobaPlus;

            Position pos = l.getPosition(p);
            score += (Defense.getPositionFactor(pos) * p.getDefensiveRatings().getPositionScore(pos));

            /*if (p.canPlay(l.getPosition(p))) {
                score += wobaPlus;
            }*/
        }

        return pct * score;
    }

}
