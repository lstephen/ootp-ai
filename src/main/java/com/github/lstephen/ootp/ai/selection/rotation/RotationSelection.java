// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   RotationSelection.java
package com.github.lstephen.ootp.ai.selection.rotation;

import com.github.lstephen.ootp.ai.io.Printables;
import com.github.lstephen.ootp.ai.player.Player;
import com.github.lstephen.ootp.ai.player.Slot;
import com.github.lstephen.ootp.ai.selection.Mode;
import com.github.lstephen.ootp.ai.selection.Selection;
import com.github.lstephen.ootp.ai.selection.rotation.Rotation.Role;
import com.github.lstephen.ootp.ai.stats.PitcherOverall;
import com.github.lstephen.ootp.ai.stats.PitchingStats;
import com.github.lstephen.ootp.ai.stats.TeamStats;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.github.lstephen.ai.search.HillClimbing;
import com.github.lstephen.ai.search.RepeatedHillClimbing;
import com.github.lstephen.ai.search.Validator;
import com.github.lstephen.ai.search.action.Action;
import com.github.lstephen.ai.search.action.ActionGenerator;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.*;

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

        System.out.println("Selecting rotation...");
        System.out.println("Forced:" + Iterables.size(forced));
        System.out.println("Available:" + Iterables.size(available));

        HillClimbing<Rotation> hc = HillClimbing
            .<Rotation>builder()
            .validator(r -> r.isValid() && r.get(Role.SP).size() == definition.getRotationSize())
            .heuristic(heuristic())
            .actionGenerator(actionGenerator(forced, available))
            .build();

        Rotation r = new RepeatedHillClimbing<Rotation>(
              initialStateGenerator(forced, available), hc)
            .search();

        for (Player p : Player.byShortName().sortedCopy(r.getAll())) {
            System.out.print(p.getShortName() + "/");
        }
        System.out.println();
        return r;
    }

    private Ordering<Rotation> heuristic() {
      Ordering<Rotation> byOverall = Ordering
        .natural()
        .onResultOf(new Function<Rotation, Double>() {
          public Double apply(Rotation r) {
            Double score = 0.0;
            for (Player p : r.getAll()) {
              score += method.getPlus(predictions.getOverall(p));
            }
            return score;
          }});

      return Ordering
        .natural()
        .onResultOf(new Function<Rotation, Double>() {
            public Double apply(Rotation r) {
                return r.score(predictions, method);
            }
        })
        .compound(byOverall);
    }


    private Supplier<Rotation> initialStateGenerator(final Iterable<Player> forced, final Iterable<Player> available) {
        return new Supplier<Rotation>() {
            @Override
            public Rotation get() {
                List<Player> ps = Lists.newArrayList(available);
                Iterables.removeAll(ps, ImmutableSet.copyOf(forced));

                Collections.shuffle(ps);

                List<Player> sps = Lists.newArrayList();
                List<Player> mrs = Lists.newArrayList();
                List<Player> rest = Lists.newArrayList();

                for (Player p : forced) {
                    if (p.getSlots().contains(Slot.SP) && sps.size() < definition.getRotationSize()) {
                        sps.add(p);
                    } else if (mrs.size() < definition.getRelieversSize()) {
                        mrs.add(p);
                    } else {
                        rest.add(p);
                    }
                }

                for (Player p : ps) {
                    if (p.getSlots().contains(Slot.SP) && sps.size() < definition.getRotationSize()) {
                        sps.add(p);
                    }
                }

                ps.removeAll(sps);

                sps.addAll(FluentIterable
                    .from(ps)
                    .limit(definition.getRotationSize() - sps.size())
                    .toList());

                ps.removeAll(sps);

                mrs.addAll(FluentIterable
                    .from(ps)
                    .limit(definition.getRelieversSize() - mrs.size())
                    .toList());

                ps.removeAll(mrs);

                rest.addAll(FluentIterable
                    .from(ps)
                    .limit(Math.max(0, slots.size() - sps.size() - mrs.size() - rest.size()))
                    .toList());

                Rotation initial = Rotation.create(sps, mrs, rest);

                if (!initial.isValid()) {
                    Printables.print(initial).to(System.out);
                }

                Preconditions.checkState(initial.isValid());

                return initial;
            }
        };
    }

    private ActionGenerator<Rotation> actionGenerator(final Iterable<Player> forced, final Iterable<Player> available) {
        return new ActionGenerator<Rotation>() {
            @Override
            public Stream<Action<Rotation>> apply(Rotation r) {
                Iterable<Action<Rotation>> actions =
                    Iterables.<Action<Rotation>>concat(
                        swaps(r),
                        substitutions(r),
                        moves(r));

                return Stream.concat(swaps(r).stream(), Stream.concat(substitutions(r).stream(), moves(r).stream()));
            }

            private Set<Move> moves(Rotation rot) {
              Set<Move> moves = Sets.newHashSet();

              for (Player p : rot.getAll()) {
                for (Role r : Rotation.Role.values()) {
                  for (int i = 0; i < 5; i++) {
                    moves.add(new Move(p, r, i));
                  }
                }
              }

              return moves;
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
            case IDEAL:
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

    private static class Move implements Action<Rotation> {
      private Player p;
      private Rotation.Role role;
      private Integer idx;

      public Move(Player p, Rotation.Role role, Integer idx) {
        this.p = p;
        this.role = role;
        this.idx = idx;
      }

      public Rotation apply(Rotation r) {
        return r.move(p, role, idx);
      }
    }
}
