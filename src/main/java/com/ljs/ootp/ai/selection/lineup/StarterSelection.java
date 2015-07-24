package com.ljs.ootp.ai.selection.lineup;

import com.ljs.ootp.ai.player.Player;
import com.ljs.ootp.ai.player.Slot;
import com.ljs.ootp.ai.player.ratings.Position;
import com.ljs.ootp.ai.stats.BattingStats;
import com.ljs.ootp.ai.stats.TeamStats;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.logging.Logger;
import java.util.stream.Stream;

import com.github.lstephen.ai.search.HillClimbing;
import com.github.lstephen.ai.search.RepeatedHillClimbing;
import com.github.lstephen.ai.search.Validator;
import com.github.lstephen.ai.search.action.Action;
import com.github.lstephen.ai.search.action.ActionGenerator;
import com.github.lstephen.ai.search.action.SequencedAction;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicates;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;

import org.apache.commons.lang3.tuple.Pair;

// Referenced classes of package com.ljs.scratch.ootp.selection.lineup:
//            Lineup
public class StarterSelection {

    private static final Logger LOG =
        Logger.getLogger(StarterSelection.class.getName());

    private final TeamStats<BattingStats> predictions;

    private boolean requireBackupCatcher = true;

    private static final Map<Pair<Pair<Lineup.VsHand, Boolean>, ImmutableSet<Player>>, Defense> CACHE = Maps.newHashMap();

    public StarterSelection(TeamStats<BattingStats> predictions) {
        this.predictions = predictions;
    }

    public StarterSelection dontRequireBackupCatcher() {
        requireBackupCatcher = false;
        return this;
    }

    public Iterable<Player> selectWithDh(
        Lineup.VsHand vs, Iterable<Player> available) {

        Set<Player> selected = Sets.newHashSet(select(vs, available));
        selected.add(
            selectDh(
                vs,
                Sets.difference(ImmutableSet.copyOf(available), selected)));
        return selected;
    }

    private Player selectDh(Lineup.VsHand vs, Iterable<Player> available) {
        if (Iterables.size(available) == 1) {
            return available.iterator().next();
        }

        for (Player p : byWoba(vs).sortedCopy(available)) {
            if (!requireBackupCatcher) {
                return p;
            } else {
                if ((!p.getSlots().contains(Slot.C))
                    || containsCatcher(
                        Sets.difference(ImmutableSet.copyOf(available),
                        ImmutableSet.of(p)))) {

                    return p;
                }
            }
        }

        throw new IllegalStateException();
    }

    public Iterable<Player> select(
        Lineup.VsHand vs, Iterable<Player> available) {

        Pair<Pair<Lineup.VsHand, Boolean>, ImmutableSet<Player>> cacheKey =
            Pair.of(Pair.of(vs, requireBackupCatcher), ImmutableSet.copyOf(available));

        if (!CACHE.containsKey(cacheKey)) {

            HillClimbing<Defense> hc = HillClimbing
                .<Defense>builder()
                .actionGenerator(actionsFunction(available))
                .heuristic(heuristic(vs))
                .validator(validator(available))
                .build();

            CACHE.put(
                cacheKey,
                new RepeatedHillClimbing<Defense>(
                    Defense.randomGenerator(available),
                    hc)
                .search());
        }

        return CACHE.get(cacheKey).players();
    }

    private Validator<Defense> validator(final Iterable<Player> available) {
        Integer cc = 0;
        for (Player p : available) {
          if (p.canPlay(Position.CATCHER)) {
            cc++;
          }
        }

        final Integer catcherCount = cc;

        return new Validator<Defense>() {
            @Override
            public boolean test(Defense input) {
                if (requireBackupCatcher) {
                    return catcherCount > 1
                      && containsCatcher(
                          FluentIterable
                            .from(available)
                            .filter(Predicates.not(Predicates.in(input.players()))));
                } else {
                    return true;
                }
            }
        };

    }

    private Ordering<Defense> heuristic(final Lineup.VsHand vs) {
        return Ordering
            .natural()
            .onResultOf(new Function<Defense, Double>() {
                public Double apply(Defense d) {
                    Double score = 0.0;

                    for (Player p : d.players()) {
                        score +=
                            vs.getStats(predictions, p).getWobaPlus();
                    }

                    score += (d.score());

                    return score;
                }
            })
            .compound(Defense.byAge().reverse());
    }

    private boolean containsCatcher(Iterable bench) {
        for (Iterator i$ = bench.iterator(); i$.hasNext();) {
            Player p = (Player) i$.next();
            if (p.getDefensiveRatings().getPositionScore(
                Position.CATCHER).doubleValue() > 0.0D) {
                return true;
            }
        }

        return false;
    }

    private Ordering<Player> byWoba(final Lineup.VsHand vs) {
        return Ordering.natural().reverse().onResultOf(
            new Function<Player, Integer>() {
            public Integer apply(Player p) {
                return vs.getStats(predictions, p).getWobaPlus();
            }
        }).compound(Player.byTieBreak());
    }

    private ActionGenerator<Defense> actionsFunction(final Iterable<Player> available) {

        return new ActionGenerator<Defense>() {
            @Override
            public Stream<Action<Defense>> apply(Defense state) {
                final Set<Action<Defense>> swaps = Sets.newHashSet();
                Set<Action<Defense>> internalSwaps = Sets.newHashSet();


                for (Player lhs : state.players()) {
                    for (Player rhs : state.players()) {
                        internalSwaps.add(new Swap(lhs, rhs));
                    }
                    for (Player rhs : available) {
                        swaps.add(new Swap(lhs, rhs));
                    }
                }

                return Stream.concat(Stream.concat(swaps.stream(), internalSwaps.stream()), SequencedAction.merged(internalSwaps, swaps));
            }
        };
    }

    private static class Swap implements Action<Defense> {

        private final Player lhs;
        private final Player rhs;

        public Swap(Player lhs, Player rhs) {
            Preconditions.checkNotNull(lhs);
            Preconditions.checkNotNull(rhs);

            this.lhs = lhs;
            this.rhs = rhs;
        }

        public Defense apply(Defense d) {
            return d.swap(lhs, rhs);
        }
    }

}
