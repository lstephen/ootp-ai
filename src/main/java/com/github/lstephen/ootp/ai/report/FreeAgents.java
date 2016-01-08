package com.github.lstephen.ootp.ai.report;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;

import com.github.lstephen.ootp.ai.player.Player;
import com.github.lstephen.ootp.ai.player.Slot;
import com.github.lstephen.ootp.ai.roster.Changes;
import com.github.lstephen.ootp.ai.selection.Mode;
import com.github.lstephen.ootp.ai.site.Site;
import com.github.lstephen.ootp.ai.site.Version;
import com.github.lstephen.ootp.ai.value.TradeValue;

import java.util.List;
import java.util.Set;

/**
 *
 * @author lstephen
 */
public final class FreeAgents {

    private final Site site;

    private final Set<Player> skipped = Sets.newHashSet();

    private final ImmutableSet<Player> fas;

    private final Function<Player, Integer> value;

    private TradeValue tv;

    private FreeAgents(Site site, Iterable<Player> fas, Function<Player, Integer> value, TradeValue tv) {
        this.site = site;
        this.fas = ImmutableSet.copyOf(Iterables.filter(fas, Predicates.notNull()));
        this.value = value;
        this.tv = tv;
    }

    public Iterable<Player> all() {
        return fas;
    }

    public void skip(Player p) {
        skipped.add(p);
    }

    public void skip(Iterable<Player> ps) {
        for (Player p : ps) {
            skip(p);
        }
    }

    public Optional<Player> getPlayerToRelease(Iterable<Player> roster) {

        Set<Slot> needed = RosterReport.create(site, roster).getNeededSlots();

        for (Player r : byValue(value).sortedCopy(roster)) {
            if (!Sets.intersection(ImmutableSet.copyOf(r.getSlots()), needed).isEmpty()) {
                continue;
            }

            return Optional.of(r);
        }

        return Optional.absent();
    }

    public Iterable<Player> getTopTargets(Mode mode) {
        Set<Player> targets = Sets.newHashSet();

        List<Player> ps = byValue(value).reverse().sortedCopy(fas);
        Set<Slot> remaining = Sets.newHashSet(Slot.values());

        while (!remaining.isEmpty() && !ps.isEmpty()) {
            Player p = ps.get(0);

            ps.remove(p);

            if (skipPlayer(p)) {
                continue;
            }

            if (mode == Mode.PRESEASON && tv.getCurrentValueVsReplacement(p) < 0) {
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
            .onResultOf(value)
            .compound(Player.byTieBreak());
    }

    public Boolean skipPlayer(Player fa) {
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
