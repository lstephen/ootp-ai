/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.ljs.ootp.ai.selection;

import com.google.common.base.Function;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multiset;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.ljs.ootp.ai.player.Player;
import com.ljs.ootp.ai.player.Slot;
import com.ljs.ootp.ai.selection.lineup.Lineup;
import com.ljs.ootp.ai.selection.lineup.StarterSelection;
import com.ljs.ootp.ai.stats.BattingStats;
import com.ljs.ootp.ai.stats.TeamStats;
import java.util.Set;

/**
 *
 * @author levi.stephen
 */
public class BestStartersSelection implements Selection {

	private Multiset<Slot> slots;

	private final TeamStats<BattingStats> predictions;

	private final Function<Player, Integer> value;

	public BestStartersSelection(Iterable<Slot> slots, TeamStats<BattingStats> predictions, Function<Player, Integer> value) {
		this.slots = HashMultiset.create(slots);
		this.predictions = predictions;
		this.value = value;
	}

	@Override
	public ImmutableMultimap<Slot, Player> select(Iterable<Player> forced, Iterable<Player> available) {
		SlotAssignments assignments = SlotAssignments.create(slots);

		assignments.assign(Selections.onlyHitters(forced));

		assignBestStarters(assignments, Selections.onlyHitters(available));

		Multiset<Slot> remaining = assignments.getRemainingSlots();

		if (!remaining.isEmpty()) {
			return SlotSelection
				.builder()
				.slots(slots)
				.ordering(byOverall())
				.size(slots.size())
				.build()
				.select(assignments.getAssignedPlayers(), available);

		} else {
			return assignments.toMap();
		}
	}

	private void assignBestStarters(SlotAssignments assignments, Iterable<Player> available) {
		Set<Player> remaining = Sets.newHashSet(Iterables.concat(assignments.getAssignedPlayers(), available));

		StarterSelection starters = new StarterSelection(predictions);

		Set<Player> selected = Sets.newHashSet(
			Iterables.concat(
				starters.selectWithDh(Lineup.VsHand.VS_LHP, remaining),
				starters.selectWithDh(Lineup.VsHand.VS_RHP, remaining)));

		assignments.attemptToAssign(byOverall().sortedCopy(selected));

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

}
