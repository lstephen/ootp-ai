package com.ljs.ootp.ai.report;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.ljs.ootp.ai.roster.Changes;
import com.ljs.ootp.ai.player.Player;
import com.ljs.ootp.ai.player.Slot;
import com.ljs.ootp.ai.selection.Mode;
import com.ljs.ootp.ai.site.Site;
import com.ljs.ootp.ai.site.Version;
import com.ljs.ootp.ai.value.FreeAgentAcquisition;
import com.ljs.ootp.ai.value.TradeValue;
import java.util.Set;

/**
 *
 * @author lstephen
 */
public final class FreeAgents {

    private final Site site;

    private final Set<Player> skipped = Sets.newHashSet();

    private final Iterable<Player> fas;

    private final Function<Player, Integer> value;

    private TradeValue tv;

    private FreeAgents(Site site, Iterable<Player> fas, Function<Player, Integer> value, TradeValue tv) {
        this.site = site;
        this.fas = fas;
        this.value = value;
        this.tv = tv;
    }

    public void skip(Player p) {
        skipped.add(p);
    }

    public void skip(Iterable<Player> ps) {
        for (Player p : ps) {
            skip(p);
        }
    }

    public Optional<FreeAgentAcquisition> getTopAcquisition(
        Iterable<Player> roster) {

        for (Player fa : byValue(value).sortedCopy(fas)) {
            if (skipPlayer(fa)) {
                continue;
            }

            Optional<Player> release = getPlayerToReleaseFor(roster, fa);

            if (release.isPresent()) {
                return Optional.of(
                    FreeAgentAcquisition.create(fa, release.get()));
            }
        }

        return Optional.absent();
    }

    public Optional<FreeAgentAcquisition> getNeedAcquisition(
        Iterable<Player> roster) {

        Set<Slot> needed = RosterReport.create(site, roster).getNeededSlots();

        for (Player fa : byValue(value).sortedCopy(fas)) {
            if (skipPlayer(fa)) {
                continue;
            }

            if (needed.contains(Slot.getPrimarySlot(fa))) {
                Optional<Player> release = getPlayerToReleaseFor(roster, fa);

                if (release.isPresent()) {
                    return Optional.of(
                        FreeAgentAcquisition.create(fa, release.get()));
                }
            }
        }

        return Optional.absent();
    }

    private Optional<Player> getPlayerToReleaseFor(
        Iterable<Player> roster, Player fa) {

        Set<Slot> surplus = RosterReport.create(site, roster).getSurplusSlots();
        Set<Slot> needed = RosterReport.create(site, roster).getNeededSlots();

        for (Player r : byValue(value).reverse().sortedCopy(roster)) {
            if (!fa.getSlots().contains(Slot.getPrimarySlot(r)) && !surplus.contains(Slot.getPrimarySlot(r))) {
                continue;
            }
            Set<Slot> needsFulfilled = Sets.intersection(ImmutableSet.copyOf(Slot.getPlayerSlots(r)), needed);

            boolean fillsNeed = !needsFulfilled.isEmpty();
            boolean fillsReplaceableNeed = needsFulfilled.size() == 1 && needsFulfilled.iterator().next() == Slot.getPrimarySlot(fa);
            boolean occupiesSameSlot = Slot.getPrimarySlot(fa) == Slot.getPrimarySlot(r);

            if (fillsNeed && !fillsReplaceableNeed && !occupiesSameSlot) {
                continue;
            }
            if ((int) (value.apply(r) * 1.1) > value.apply(fa)) {
                break;
            }

            return Optional.of(r);
        }

        return Optional.absent();
    }

    public Optional<Player> getPlayerToRelease(Iterable<Player> roster) {

        Set<Slot> needed = RosterReport.create(site, roster).getNeededSlots();

        for (Player r : byValue(value).reverse().sortedCopy(roster)) {
            if (!Sets.intersection(ImmutableSet.copyOf(r.getSlots()), needed).isEmpty()) {
                continue;
            }

            return Optional.of(r);
        }

        return Optional.absent();
    }

    public Iterable<Player> getTopTargets(Mode mode) {
        Set<Player> targets = Sets.newHashSet();

        Set<Slot> remaining = Sets.newHashSet(Slot.values());

        for (Player p : byValue(value).sortedCopy(fas)) {
            if (skipPlayer(p)) {
                continue;
            }
            if (site.getType() != Version.OOTPX && mode == Mode.PRESEASON && tv.getCurrentValueVsReplacement(p) < 0) {
                continue;
            }

            for (Slot s : p.getSlots()) {
                if (remaining.contains(s)) {
                    targets.add(p);
                    remaining.remove(s);
                    break;
                }
            }
        }

        return targets;
    }

    private static Ordering<Player> byValue(Function<Player, Integer> value) {
        return Ordering
            .natural()
            .reverse()
            .onResultOf(value)
            .compound(Player.byTieBreak());
    }

    private Boolean skipPlayer(Player fa) {
        if (skipped.contains(fa)) {
            return true;
        }
        if (fa.getShortName().contains("fake ") || fa.getShortName().contains("Draft Pik")) {
            return true;
        }
        if (fa.getTeam() != null && fa.getTeam().contains("*CEI*")) {
            return true;
        }
        return false;
    }

    public static FreeAgents create(Site site, Changes changes, Function<Player, Integer> value, TradeValue tv) {
        FreeAgents fas = create(site, site.getFreeAgents(), value, tv);

        fas.skip(changes.get(Changes.ChangeType.DONT_ACQUIRE));

        return fas;
    }

    public static FreeAgents create(Site site, Iterable<Player> fas, Function<Player, Integer> value, TradeValue tv) {
        return new FreeAgents(site, fas, value, tv);
    }

}
