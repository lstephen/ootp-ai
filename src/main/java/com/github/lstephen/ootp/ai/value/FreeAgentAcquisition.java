package com.github.lstephen.ootp.ai.value;

import com.github.lstephen.ootp.ai.player.Player;
import com.github.lstephen.ootp.ai.player.Slot;
import com.github.lstephen.ootp.ai.report.RosterReport;
import com.github.lstephen.ootp.ai.roster.Changes;
import com.github.lstephen.ootp.ai.roster.Changes.ChangeType;
import com.github.lstephen.ootp.ai.site.Site;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;

import com.github.lstephen.ai.search.Heuristic;
import com.github.lstephen.ai.search.HillClimbing;
import com.github.lstephen.ai.search.RepeatedHillClimbing;
import com.github.lstephen.ai.search.action.Action;
import com.github.lstephen.ai.search.action.ActionGenerator;

/**
 *
 * @author lstephen
 */
public final class FreeAgentAcquisition {

    private final Player fa;

    private final Player release;

    private FreeAgentAcquisition(Player fa, Player release) {
        this.fa = fa;
        this.release = release;
    }

    private FreeAgentAcquisition withFreeAgent(Player fa) {
        return new FreeAgentAcquisition(fa, release);
    }

    private FreeAgentAcquisition withRelease(Player release) {
        return new FreeAgentAcquisition(fa, release);
    }

    public Player getFreeAgent() {
        return fa;
    }

    public Player getRelease() {
        return release;
    }

    public Integer score(RosterReport rr, TradeValue value) {
        Integer score = 0;

        score += value.getTradeTargetValue(fa);
        score -= value.getTradeTargetValue(release);

        // Penalize for releasing player in needed slot
        for (Slot s : release.getSlots()) {
            if (s != Slot.P) {
                score -= Math.max(0, rr.getTargetRatio() - rr.getRatio(s));
            }
        }

        // Reward for acquiring player in needed slot
        score += Math.max(0, rr.getTargetRatio() - rr.getRatio(Slot.getPrimarySlot(fa)));

        return score;
    }

    public static FreeAgentAcquisition create(Player fa, Player release) {
        return new FreeAgentAcquisition(fa, release);
    }

    public static ImmutableSet<FreeAgentAcquisition> select(Site site, Changes changes, Iterable<Player> roster, Iterable<Player> fas, TradeValue value, Integer n) {
        Set<FreeAgentAcquisition> faas = Sets.newHashSet();

        Set<Player> rroster = Sets.newHashSet(roster);
        Set<Player> rfas = Sets.newHashSet(fas);

        rfas.removeAll(ImmutableSet.copyOf(changes.get(ChangeType.DONT_ACQUIRE)));

        for (int i = 0; i < n; i++) {
            Optional<FreeAgentAcquisition> faa = select(site, rroster, rfas, value);

            if (!faa.isPresent()) {
                break;
            }

            faas.add(faa.get());

            rroster.remove(faa.get().getRelease());
            rfas.remove(faa.get().getFreeAgent());
        }

        return ImmutableSet.copyOf(faas);
    }

    private static Optional<FreeAgentAcquisition> select(Site site, Iterable<Player> roster, Iterable<Player> fas, TradeValue value) {
        if (Iterables.isEmpty(fas)) {
            return Optional.absent();
        }

        final RosterReport rr = RosterReport.create(site, roster);

        HillClimbing<FreeAgentAcquisition> hc =
            HillClimbing
                .<FreeAgentAcquisition>builder()
                .heuristic(heuristic(rr, value))
                .actionGenerator(actionGenerator(roster, fas))
                .build();

        FreeAgentAcquisition faa =
            new RepeatedHillClimbing<FreeAgentAcquisition>(
                initialStateGenerator(roster, fas),
                hc)
            .search();

        if (faa.score(rr, value) > 0) {
            return Optional.of(faa);
        } else {
            return Optional.absent();
        }
    }

    private static Supplier<FreeAgentAcquisition> initialStateGenerator(final Iterable<Player> roster, final Iterable<Player> fas) {
        return new Supplier<FreeAgentAcquisition>() {
            @Override
            public FreeAgentAcquisition get() {
                List<Player> rel = Lists.newArrayList(roster);
                List<Player> acq = Lists.newArrayList(fas);

                Collections.shuffle(rel);
                Collections.shuffle(acq);

                return new FreeAgentAcquisition(acq.get(0), rel.get(0));
            }
        };
    }

    private static Heuristic<FreeAgentAcquisition> heuristic(final RosterReport rr, final TradeValue value) {
      return Ordering
        .natural()
        .onResultOf((FreeAgentAcquisition faa) -> faa.score(rr, value))::compare;
    }

    private static ActionGenerator<FreeAgentAcquisition> actionGenerator(final Iterable<Player> roster, final Iterable<Player> fas) {
        final Set<Action<FreeAgentAcquisition>> actions = Sets.newHashSet();

        for (Player p : roster) {
            actions.add(new ChangeRelease(p));
        }

        for (Player p : fas) {
            actions.add(new ChangeFreeAgent(p));
        }

        return (faa) -> actions.stream();
    }

    private static class ChangeFreeAgent implements Action<FreeAgentAcquisition> {
        private Player fa;

        public ChangeFreeAgent(Player fa) {
            this.fa = fa;
        }

        public FreeAgentAcquisition apply(FreeAgentAcquisition faa) {
            return faa.withFreeAgent(fa);
        }
    }

    private static class ChangeRelease implements Action<FreeAgentAcquisition> {
        private Player release;

        public ChangeRelease(Player release) {
            this.release = release;
        }

        public FreeAgentAcquisition apply(FreeAgentAcquisition faa) {
            return faa.withRelease(release);
        }
    }

}
