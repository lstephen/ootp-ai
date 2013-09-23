package com.ljs.scratch.ootp.value;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.ljs.scratch.ootp.core.Player;
import com.ljs.scratch.ootp.report.RosterReport;
import com.ljs.scratch.ootp.selection.Slot;
import java.util.Set;

/**
 *
 * @author lstephen
 */
public class FreeAgentAcquisition {

    private final Player fa;

    private final Player release;

    private FreeAgentAcquisition(Player fa, Player release) {
        this.fa = fa;
        this.release = release;
    }

    public Player getFreeAgent() {
        return fa;
    }

    public Player getRelease() {
        return release;
    }

    public static Optional<FreeAgentAcquisition> getTopAcquisition(
        Iterable<Player> roster, Iterable<Player> fas, Function<Player, Integer> value) {

        for (Player fa : byValue(value).sortedCopy(fas)) {
            if (skipPlayer(fa)) {
                continue;
            }

            Optional<Player> release = getPlayerToReleaseFor(roster, fa, value);

            if (release.isPresent()) {
                return Optional.of(new FreeAgentAcquisition(fa, release.get()));
            }
        }

        return Optional.absent();
    }

    public static Optional<FreeAgentAcquisition> getNeedAcquisition(
        Iterable<Player> roster, Iterable<Player> fas, Function<Player, Integer> value) {

        Set<Slot> needed = RosterReport.create(roster).getNeededSlots();

        for (Player fa : byValue(value).sortedCopy(fas)) {
            if (skipPlayer(fa)) {
                continue;
            }

            if (needed.contains(Slot.getPrimarySlot(fa))) {
                Optional<Player> release = getPlayerToReleaseFor(roster, fa, value);

                if (release.isPresent()) {
                    return Optional.of(new FreeAgentAcquisition(fa, release.get()));
                }
            }
        }

        return Optional.absent();
    }

    private static Optional<Player> getPlayerToReleaseFor(
        Iterable<Player> roster, Player fa, Function<Player, Integer> value) {

        Set<Slot> surplus = RosterReport.create(roster).getSurplusSlots();
        Set<Slot> needed = RosterReport.create(roster).getNeededSlots();

        for (Player r : byValue(value).reverse().sortedCopy(roster)) {
            if (!Slot.getPlayerSlots(fa).contains(Slot.getPrimarySlot(r)) && !surplus.contains(Slot.getPrimarySlot(r))) {
                continue;
            }
            Set<Slot> needsFulfilled = Sets.intersection(ImmutableSet.copyOf(Slot.getPlayerSlots(r)), needed);
            if (!needsFulfilled.isEmpty() && Slot.getPrimarySlot(fa) != Slot.getPrimarySlot(r)) {
                continue;
            }
            if ((int) (value.apply(r) * 1.1) > value.apply(fa)) {
                break;
            }

            return Optional.of(r);
        }

        return Optional.absent();
    }

    public static Optional<Player> getPlayerToRelease(Iterable<Player> roster, Function<Player, Integer> value) {

        Set<Slot> needed = RosterReport.create(roster).getNeededSlots();

        for (Player r : byValue(value).reverse().sortedCopy(roster)) {
            if (!Sets.intersection(ImmutableSet.copyOf(Slot.getPlayerSlots(r)), needed).isEmpty()) {
                continue;
            }

            return Optional.of(r);
        }

        return Optional.absent();
    }

    public static Iterable<Player> getTopTargets(Iterable<Player> fas, Function<Player, Integer> value) {
        Set<Player> targets = Sets.newHashSet();

        Set<Slot> remaining = Sets.newHashSet(Slot.values());

        for (Player p : byValue(value).sortedCopy(fas)) {
            if (skipPlayer(p)) {
                continue;
            }

            for (Slot s : Slot.getPlayerSlots(p)) {
                if (remaining.contains(s)) {
                    targets.add(p);
                    remaining.remove(s);
                    break;
                }
            }
        }

        return targets;
    }

    private static Boolean skipPlayer(Player fa) {
        if (fa.getShortName().contains("fake ") || fa.getShortName().contains("Draft Pik")) {
            return true;
        }
        if (fa.getTeam().contains("*CEI*")) {
            return true;
        }
        return false;
    }

    private static Ordering<Player> byValue(Function<Player, Integer> value) {
        return Ordering
            .natural()
            .reverse()
            .onResultOf(value)
            .compound(Player.byAge());
    }

    public final static class Meta {
        private Meta() { }

        public static Function<FreeAgentAcquisition, Player> getRelease() {
            return new Function<FreeAgentAcquisition, Player>() {
                @Override
                public Player apply(FreeAgentAcquisition faa) {
                    return faa.getRelease();
                }
            };
        }

    }

}
