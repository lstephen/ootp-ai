// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   RotationSelection.java
package com.ljs.ootp.ai.selection.rotation;

import com.google.common.base.Function;
import com.google.common.collect.*;
import com.ljs.ai.search.hillclimbing.HillClimbing;
import com.ljs.ai.search.hillclimbing.RepeatedHillClimbing;
import com.ljs.ai.search.hillclimbing.Validator;
import com.ljs.ai.search.hillclimbing.action.Action;
import com.ljs.ai.search.hillclimbing.action.ActionGenerator;
import com.ljs.ootp.ai.player.Player;
import com.ljs.ootp.ai.player.Slot;
import com.ljs.ootp.ai.selection.Mode;
import com.ljs.ootp.ai.selection.Selection;
import com.ljs.ootp.ai.stats.PitcherOverall;
import com.ljs.ootp.ai.stats.PitchingStats;
import com.ljs.ootp.ai.stats.TeamStats;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

// Referenced classes of package com.ljs.scratch.ootp.selection.rotation:
//            Rotation
public final class RotationSelection implements Selection {

    private final TeamStats<PitchingStats> predictions;

    private final PitcherOverall method;

    private final RotationDefinition definition;

    private final Multiset<Slot> slots;

    private RotationSelection(
        TeamStats<PitchingStats> predictions,
        PitcherOverall method,
        RotationDefinition definition,
        Multiset<Slot> slots) {

        this.predictions = predictions;
        this.method = method;
        this.definition = definition;
        this.slots = slots;
    }

    public ImmutableMultimap<Slot, Player> select(
        Iterable<Player> forced, Iterable<Player> available) {

        Rotation r = selectRotation(forced, available);

        Multimap<Slot, Player> assigned = HashMultimap.create();

        for (Player p : r.getStarters()) {
            assigned.put(Slot.SP, p);
        }

        for (Player p : r.getNonStarters()) {
            assigned.put(Slot.MR, p);
        }

        return ImmutableMultimap.copyOf(assigned);
    }

    public Rotation selectRotation(Iterable<Player> forced, Iterable<Player> available) {

        HillClimbing.Builder<Rotation> builder = HillClimbing
            .<Rotation>builder()
            .validator(new Validator<Rotation>() {
                public Boolean apply(Rotation r) {
                    return r.isValid();
                }
            })
            .heuristic(Ordering
                .natural()
                .onResultOf(new Function<Rotation, Double>() {
                    public Double apply(Rotation r) {
                        return r.score(predictions, method);
                    }
                }))
            .actionGenerator(actionGenerator(forced, available));

        return new RepeatedHillClimbing<Rotation>(
                initialStateGenerator(forced, available),
                builder)
            .search();
    }

    private Callable<Rotation> initialStateGenerator(final Iterable<Player> forced, final Iterable<Player> available) {
        return new Callable<Rotation>() {
            @Override
            public Rotation call() throws Exception {
                List<Player> ps = Lists.newArrayList(available);
                Iterables.removeAll(ps, ImmutableSet.copyOf(forced));

                Collections.shuffle(ps);

                List<Player> sps = Lists.newArrayList();

                for (Player p : forced) {
                    if (p.getSlots().contains(Slot.SP)) {
                        sps.add(p);
                    }
                }

                sps.addAll(ps.subList(0, definition.getRotationSize() - sps.size()));
                ps.removeAll(sps);

                List<Player> mrs = Lists.newArrayList();

                for (Player p : forced) {
                    if (!sps.contains(p)) {
                        mrs.add(p);
                    }
                }

                mrs.addAll(ps.subList(0, definition.getRelieversSize() - mrs.size()));
                ps.removeAll(mrs);

                Iterable<Player> rest = FluentIterable
                    .from(ps)
                    .limit(slots.size() - definition.getRotationSize() - definition.getRelieversSize());

                return Rotation.create(sps, mrs, rest);
            }
        };
    }

    private ActionGenerator<Rotation> actionGenerator(final Iterable<Player> forced, final Iterable<Player> available) {
        return new ActionGenerator<Rotation>() {
            @Override
            public Iterable<Action<Rotation>> apply(Rotation r) {
                Set<Substitute> subs = substitutions(r);
                Set<Swap> swaps = swaps(r);

                Iterable<Action<Rotation>> actions =
                    Iterables.<Action<Rotation>>concat(
                        swaps(r),
                        substitutions(r));
                        //SequencedAction.allPairs(subs));

                /*synchronized (RotationSelection.this) {
                    PrintWriter w = new PrintWriter(System.out);
                    w.println("----------");
                    r.print(w);
                    w.println("Score: " + r.score(predictions, method));
                    w.println("Actions: " + Iterables.size(actions));
                    w.flush();
                }*/

                return actions;
            }

            private Set<Swap> swaps(Rotation r) {
                Set<Swap> swaps = Sets.newHashSet();

                ImmutableList<Player> ps = ImmutableList.copyOf(r.getAll());

                for (int lhs = 0; lhs < ps.size(); lhs++) {
                    for (int rhs = lhs + 1; rhs < ps.size(); rhs++) {
                        swaps.add(new Swap(ps.get(lhs), ps.get(rhs)));
                    }
                }

                return swaps;
            }

            private Set<Substitute> substitutions(Rotation r) {
                Set<Substitute> ss = Sets.newHashSet();

                List<Player> ps = Lists.newArrayList(r.getAll());

                Iterables.removeAll(ps, ImmutableSet.copyOf(forced));

                for (Player in : available) {
                    if (!ps.contains(in)) {
                        for (Player out : ps) {
                            ss.add(new Substitute(in, out));
                        }
                    }
                }

                return ss;
            }


        };
    }

    public static RotationSelection regularSeason(
        TeamStats<PitchingStats> pitching, PitcherOverall method) {

        return new RotationSelection(
            pitching, method, RotationDefinition.regularSeason(), Mode.REGULAR_SEASON.getPitchingSlots());
    }

    public static RotationSelection expanded(
        TeamStats<PitchingStats> pitching, PitcherOverall method) {

        return new RotationSelection(
            pitching, method, RotationDefinition.regularSeason(), Mode.EXPANDED.getPitchingSlots());
    }

    public static RotationSelection playoffs(
        TeamStats<PitchingStats> pitching, PitcherOverall method) {

        return new RotationSelection(
            pitching, method, RotationDefinition.playoffs(), Mode.PLAYOFFS.getPitchingSlots());
    }

    public static RotationSelection forMode(
        Mode mode, TeamStats<PitchingStats> pitching, PitcherOverall method) {

        switch (mode) {
            case PRESEASON:
            case REGULAR_SEASON:
                return regularSeason(pitching, method);
            case EXPANDED:
                return expanded(pitching, method);
            case PLAYOFFS:
                return playoffs(pitching, method);
            default:
                throw new IllegalStateException();
        }
    }

    private static final class RotationDefinition {

        public Integer getRotationSize() {
            return rotation;
        }

        public Integer getRelieversSize() {
            return relievers;
        }

        public static RotationDefinition regularSeason() {
            return new RotationDefinition(REGULAR_SEASON_ROTATION, RELIEVERS);
        }

        public static RotationDefinition playoffs() {
            return new RotationDefinition(PLAYOFFS_ROTATION, RELIEVERS);
        }
        private static final Integer REGULAR_SEASON_ROTATION = Integer
            .valueOf(5);

        private static final Integer PLAYOFFS_ROTATION = Integer.valueOf(4);

        private static final Integer RELIEVERS = Integer.valueOf(4);

        private final Integer rotation;

        private final Integer relievers;

        private RotationDefinition(Integer rotation, Integer relievers) {
            this.rotation = rotation;
            this.relievers = relievers;
        }
    }

    private static class Swap implements Action<Rotation> {

        private Player lhs;
        private Player rhs;

        public Swap(Player lhs, Player rhs) {
            this.lhs = lhs;
            this.rhs = rhs;
        }

        public Rotation apply(Rotation r) {
            return r.swap(lhs, rhs);
        }
    }

    private static class Substitute implements Action<Rotation> {

        private Player in;
        private Player out;

        public Substitute(Player in, Player out) {
            this.in = in;
            this.out = out;
        }

        public Rotation apply(Rotation r) {
            return r.substitute(in, out);
        }
    }
}
