/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.ljs.ootp.ai.selection;

import com.google.common.base.Function;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multiset;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.ljs.ootp.ai.player.Player;
import com.ljs.ootp.ai.player.Slot;
import com.ljs.ootp.ai.selection.bench.Bench;
import com.ljs.ootp.ai.selection.lineup.AllLineups;
import com.ljs.ootp.ai.selection.lineup.Lineup;
import com.ljs.ootp.ai.selection.lineup.LineupSelection;
import com.ljs.ootp.ai.selection.lineup.StarterSelection;
import com.ljs.ootp.ai.stats.BattingStats;
import com.ljs.ootp.ai.stats.SplitPercentages;
import com.ljs.ootp.ai.stats.TeamStats;
import java.io.PrintWriter;
import java.util.Set;

/**
 *
 * @author levi.stephen
 */
public class BestStartersSelection implements Selection {

    private static SplitPercentages pcts;

	private final Multiset<Slot> slots;

	private final TeamStats<BattingStats> predictions;

	private final Function<Player, Integer> value;

	public BestStartersSelection(Iterable<Slot> slots, TeamStats<BattingStats> predictions, Function<Player, Integer> value) {
		this.slots = HashMultiset.create(slots);
		this.predictions = predictions;
		this.value = value;
	}

    public static void setPercentages(SplitPercentages pcts) {
        BestStartersSelection.pcts = pcts;
    }

	@Override
	public ImmutableMultimap<Slot, Player> select(Iterable<Player> forced, Iterable<Player> available) {
		SlotAssignments assignments = SlotAssignments.create(slots);

		assignments.assign(Selections.onlyHitters(forced));

		assignBestStarters(assignments, Selections.onlyHitters(available));

		Multiset<Slot> remaining = assignments.getRemainingSlots();

        if (remaining.isEmpty()) {
            return assignments.toMap();
        } else {
            AllLineups lineups = new LineupSelection(predictions).select(assignments.getAssignedPlayers());

            PrintWriter w = new PrintWriter(System.out);
            lineups.print(w);
            w.println(lineups.getAllPlayers().size() + " "  + assignments.toMap().size() + assignments.getAssignedPlayers().size());
            w.flush();

            Multimap<Slot, Player> result = HashMultimap.create(assignments.toMap());

            Bench bench = Bench.select(lineups, predictions, available, slots.size());

            for (Player p : bench.players()) {
                result.put(Slot.getPrimarySlot(p), p);
            }

            return ImmutableMultimap.copyOf(result);
		}
	}

	private void assignBestStarters(SlotAssignments assignments, Iterable<Player> available) {
		Set<Player> remaining = Sets.newHashSet(Iterables.concat(assignments.getAssignedPlayers(), available));

		StarterSelection starters = new StarterSelection(predictions);

		Set<Player> selected = Sets.newHashSet(
			Iterables.concat(
				starters.selectWithDh(Lineup.VsHand.VS_LHP, remaining),
				starters.selectWithDh(Lineup.VsHand.VS_RHP, remaining)));

        AllLineups lineups = new LineupSelection(predictions).select(selected);

		assignments.attemptToAssign(byValueProvided(lineups).sortedCopy(selected));

		if (!assignments.containsAll(selected) && !assignments.getRemainingSlots().isEmpty()) {
			remaining.removeAll(selected);

			if (!remaining.isEmpty()) {
				assignBestStarters(assignments, remaining);
			}
		}
	}

	private Ordering<Player> byOverall() {
		return Ordering
			.natural()
			.reverse()
			.onResultOf(value)
			.compound(Player.byTieBreak());
	}

    private Ordering<Player> byValueProvided(final AllLineups lineups) {
        return Ordering
            .natural()
            .reverse()
            .onResultOf(new Function<Player, Integer>() {
                public Integer apply(Player p) {
                    return getValueProvided(p, lineups);
                }
            })
            .compound(Player.byTieBreak());
    }

    private Integer getValueProvided(Player p, AllLineups lineups) {
        Integer score = value.apply(p);

        score += getValueProvided(p, lineups.getVsLhp(), pcts.getVsLhpPercentage(), Lineup.VsHand.VS_LHP);
        score += getValueProvided(p, lineups.getVsLhpPlusDh(), pcts.getVsLhpPercentage(), Lineup.VsHand.VS_LHP);
        score += getValueProvided(p, lineups.getVsRhp(), pcts.getVsRhpPercentage(), Lineup.VsHand.VS_RHP);
        score += getValueProvided(p, lineups.getVsRhpPlusDh(), pcts.getVsRhpPercentage(), Lineup.VsHand.VS_RHP);

        return score;
    }

    private Integer getValueProvided(Player p, Lineup l, Double pct, Lineup.VsHand vs) {
        Integer score = 0;
        Integer wobaPlus = vs.getStats(predictions, p).getWobaPlus();

        if (l.contains(p)) {
            score += wobaPlus;

            if (p.canPlay(l.getPosition(p))) {
                score += wobaPlus;
            }
        }

        return (int) (pct * score);
    }

}
