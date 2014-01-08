package com.ljs.ootp.ai.value;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.ljs.ai.search.hillclimbing.Heuristic;
import com.ljs.ai.search.hillclimbing.Heuristics;
import com.ljs.ai.search.hillclimbing.HillClimbing;
import com.ljs.ai.search.hillclimbing.RepeatedHillClimbing;
import com.ljs.ai.search.hillclimbing.Validators;
import com.ljs.ai.search.hillclimbing.action.Action;
import com.ljs.ai.search.hillclimbing.action.ActionGenerator;
import com.ljs.ootp.ai.player.Player;
import com.ljs.ootp.ai.player.Slot;
import com.ljs.ootp.ai.report.RosterReport;
import com.ljs.ootp.ai.site.Site;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import org.fest.assertions.api.Assertions;

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
            score -= Math.max(0, rr.getTargetRatio() - rr.getRatio(s));
        }

        // Reward for acquiring player in needed slot
        score += Math.max(0, rr.getTargetRatio() - rr.getRatio(Slot.getPrimarySlot(fa)));

        return score;
    }

    public static FreeAgentAcquisition create(Player fa, Player release) {
        return new FreeAgentAcquisition(fa, release);
    }

    public static final class Meta {
        private Meta() { }

        public static Function<FreeAgentAcquisition, Player> getRelease() {
            return new Function<FreeAgentAcquisition, Player>() {
                @Override
                public Player apply(FreeAgentAcquisition faa) {
                    Assertions.assertThat(faa).isNotNull();
                    return faa.getRelease();
                }
            };
        }

    }

    public static ImmutableSet<FreeAgentAcquisition> select(Site site, Iterable<Player> roster, Iterable<Player> fas, TradeValue value, Integer n) {
        Set<FreeAgentAcquisition> faas = Sets.newHashSet();

        Set<Player> rroster = Sets.newHashSet(roster);
        Set<Player> rfas = Sets.newHashSet(fas);

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

    public static Optional<FreeAgentAcquisition> select(Site site, Iterable<Player> roster, Iterable<Player> fas, TradeValue value) {
        if (Iterables.isEmpty(fas)) {
            return Optional.absent();
        }

        final RosterReport rr = RosterReport.create(site, roster);

        HillClimbing.Builder<FreeAgentAcquisition> builder =
            HillClimbing
                .<FreeAgentAcquisition>builder()
                .validator(Validators.<FreeAgentAcquisition>alwaysTrue())
                .heuristic(heuristic(rr, value))
                .actionGenerator(actionGenerator(roster, fas));

        FreeAgentAcquisition faa =
            new RepeatedHillClimbing<FreeAgentAcquisition>(
                initialStateGenerator(roster, fas),
                builder)
            .search();

        if (faa.score(rr, value) > 0) {
            return Optional.of(faa);
        } else {
            return Optional.absent();
        }
    }

    private static Callable<FreeAgentAcquisition> initialStateGenerator(final Iterable<Player> roster, final Iterable<Player> fas) {
        return new Callable<FreeAgentAcquisition>() {
            @Override
            public FreeAgentAcquisition call() throws Exception {
                List<Player> rel = Lists.newArrayList(roster);
                List<Player> acq = Lists.newArrayList(fas);

                Collections.shuffle(rel);
                Collections.shuffle(acq);

                return new FreeAgentAcquisition(acq.get(0), rel.get(0));
            }
        };
    }

    private static Heuristic<FreeAgentAcquisition> heuristic(final RosterReport rr, final TradeValue value) {
        return Heuristics.from(
            Ordering
                .natural()
                .onResultOf(new Function<FreeAgentAcquisition, Integer>() {
                    public Integer apply(FreeAgentAcquisition faa) {
                        return faa.score(rr, value);
                    }
                }));
    }

    private static ActionGenerator<FreeAgentAcquisition> actionGenerator(final Iterable<Player> roster, final Iterable<Player> fas) {
        final Set<Action<FreeAgentAcquisition>> actions = Sets.newHashSet();

        for (Player p : roster) {
            actions.add(new ChangeRelease(p));
        }

        for (Player p : fas) {
            actions.add(new ChangeFreeAgent(p));
        }

        return new ActionGenerator<FreeAgentAcquisition>() {
            @Override
            public Iterable<Action<FreeAgentAcquisition>> apply(FreeAgentAcquisition faa) {
                return actions;
            }
        };
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
