/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.lstephen.ootp.ai.selection;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multiset;
import com.github.lstephen.ootp.ai.io.Printable;
import com.github.lstephen.ootp.ai.player.Player;
import com.github.lstephen.ootp.ai.player.Slot;
import java.io.PrintWriter;

/**
 *
 * @author levi.stephen
 */
public class SlotAssignments implements Printable {

	private Multiset<Slot> remaining;

	private Multimap<Slot, Player> assignments = ArrayListMultimap.create();

	private SlotAssignments(Iterable<Slot> slots) {
		this.remaining = HashMultiset.create(slots);
	}

	private void assign(Slot s, Player p) {
		assignments.put(s, p);
		remaining.remove(s);
	}

	public void assign(Player p) {
		attemptToAssign(p);

		if (!getAssignedPlayers().contains(p)) {
			throw new IllegalArgumentException();
		}
	}

	public void assign(Iterable<Player> ps) {
		for (Player p : ps) {
			assign(p);
		}
	}

	public void attemptToAssign(Player p) {
		if (assignments.containsValue(p)) {
			return;
		}

		for (Slot s : p.getSlots()) {
			if (remaining.contains(s)) {
				assign(s, p);
				return;
			}
		}
	}

	public void attemptToAssign(Iterable<Player> ps) {
		for (Player p : ps) {
			attemptToAssign(p);
		}
	}

	public ImmutableSet<Player> getAssignedPlayers() {
		return ImmutableSet.copyOf(assignments.values());
	}

	public ImmutableMultiset<Slot> getRemainingSlots() {
		return ImmutableMultiset.copyOf(remaining);
	}

	public boolean containsAll(Iterable<Player> ps) {
		for (Player p : ps) {
			if (!assignments.containsValue(p)) {
				return false;
			}
		}
		return true;
	}


	public ImmutableMultimap<Slot, Player> toMap() {
		return ImmutableMultimap.copyOf(assignments);
	}

	public void print(PrintWriter w) {
		for (Slot s : assignments.keySet()) {
			w.println(s);

			for (Player p :assignments.get(s)) {
				w.println(p.getShortName());
			}
		}
	}

	public static SlotAssignments create(Iterable<Slot> slots) {
		return new SlotAssignments(slots);
	}

}
