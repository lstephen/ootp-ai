package com.ljs.ootp.ai.selection.bench;

import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.ljs.ai.search.hillclimbing.HillClimbing;
import com.ljs.ai.search.hillclimbing.RepeatedHillClimbing;
import com.ljs.ai.search.hillclimbing.Validator;
import com.ljs.ai.search.hillclimbing.action.Action;
import com.ljs.ai.search.hillclimbing.action.ActionGenerator;
import com.ljs.ai.search.hillclimbing.action.SequencedAction;
import com.ljs.ootp.ai.player.Player;
import com.ljs.ootp.ai.player.ratings.Position;
import com.ljs.ootp.ai.selection.lineup.AllLineups;
import com.ljs.ootp.ai.selection.lineup.Lineup;
import com.ljs.ootp.ai.stats.BattingStats;
import com.ljs.ootp.ai.stats.SplitPercentages;
import com.ljs.ootp.ai.stats.TeamStats;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import org.fest.util.Lists;

/**
 *
 * @author lstephen
 */
public class Bench {

    private final AllLineups lineups;

    private final ImmutableSet<Player> selected;

    private final ImmutableSet<Player> players;

    private final Integer maxSize;

    private final TeamStats<BattingStats> predictions;

    private static SplitPercentages pcts;

    private Bench(AllLineups lineups, Iterable<Player> selected, Iterable<Player> players, Integer maxSize, TeamStats<BattingStats> predictions) {
        this.lineups = lineups;
        this.players = ImmutableSet.copyOf(players);
        this.selected = ImmutableSet.copyOf(selected);
        this.maxSize = maxSize;
        this.predictions = predictions;
    }

    public static void setPercentages(SplitPercentages pcts) {
        Bench.pcts = pcts;
    }

    private Bench with(Player p) {
        return new Bench(
            lineups,
            selected,
            Iterables.concat(players, ImmutableSet.of(p)),
            maxSize,
            predictions);
    }

    private Bench without(Player p) {
        return new Bench(
            lineups,
            selected,
            Iterables.filter(players, Predicates.not(Predicates.equalTo(p))),
            maxSize,
            predictions);
    }

    public ImmutableSet<Player> players() {
        return players;
    }

    private Double score() {
        Double score = 0.0;

        for (Player p : players()) {
            score += predictions.getOverall(p).getWobaPlus();
        }

        score += (pcts.getVsLhpPercentage() * score(lineups.getVsLhp(), Lineup.VsHand.VS_LHP));
        score += (pcts.getVsLhpPercentage() * score(lineups.getVsLhpPlusDh(), Lineup.VsHand.VS_LHP));
        score += (pcts.getVsRhpPercentage() * score(lineups.getVsRhp(), Lineup.VsHand.VS_RHP));
        score += (pcts.getVsRhpPercentage() * score(lineups.getVsRhpPlusDh(), Lineup.VsHand.VS_RHP));

        return score;
    }

    private Double score(Lineup lineup, Lineup.VsHand vs) {
        return new BenchScorer(predictions)
            .score(
                Iterables.concat(
                    Sets.difference(selected, lineup.playerSet()), players),
                    lineup,
                    vs);
    }

    private ImmutableList<Player> selectBenchPlayer(Lineup lineup, final Lineup.VsHand vs, Position pos) {
        Set<Player> bench = ImmutableSet.copyOf(
            Iterables.concat(
                Sets.difference(selected, lineup.playerSet()),
                players));

        ImmutableList<Player> sortedBench = Ordering
            .natural()
            .reverse()
            .onResultOf(new Function<Player, Integer>() {
                public Integer apply(Player p) {
                    return vs.getStats(predictions, p).getWobaPlus();
                }
            })
            .compound(Player.byTieBreak())
            .immutableSortedCopy(players);

        List<Player> ps = Lists.newArrayList();

        for (Player p : sortedBench) {
            if (p.canPlay(pos)) {
                ps.add(p);
            }
        }
        return ImmutableList.copyOf(ps);
    }

    private Integer totalAge() {
        Integer age = 0;

        for (Player p : players) {
            age += p.getAge();
        }

        return age;
    }

    private static Validator<Bench> validator() {
        return new Validator<Bench>() {
            @Override
            public Boolean apply(Bench b) {
                if (b.selected.size() + b.players.size() > b.maxSize) {
                    return false;
                }

                for (Player p : b.players) {
                    if (b.lineups.getAllPlayers().contains(p)) {
                        return false;
                    }
                }

                return true;
            }
        };
    }

    private static Ordering<Bench> byScore() {
        return Ordering
            .natural()
            .onResultOf(new Function<Bench, Double>() {
                public Double apply(Bench b) {
                    return b.score();
            }});
    }

    private static Ordering<Bench> byAge() {
        return Ordering
            .natural()
            .reverse()
            .onResultOf(new Function<Bench, Integer>() {
                public Integer apply(Bench b) {
                    return b.totalAge();
            }});

    }

    private static Ordering<Bench> heuristic() {
        return byScore()
            .compound(byAge());
    }

    private static ActionGenerator<Bench> actionGenerator(final Iterable<Player> available) {
        return new ActionGenerator<Bench>() {

            @Override
            public Iterable<Action<Bench>> apply(Bench b) {
                Set<Action<Bench>> actions = Sets.newHashSet();

                Set<Add> adds = adds(b);
                Set<Remove> removes = removes(b);

                actions.addAll(adds);
                actions.addAll(removes);
                actions.addAll(SequencedAction.merged(adds, removes));

                return actions;
            }

            private Set<Remove> removes(Bench b) {
                Set<Remove> removes = Sets.newHashSet();
                for (Player p : b.players) {
                    removes.add(new Remove(p));
                }
                return removes;
            }

            private Set<Add> adds(Bench b) {
                Set<Add> adds = Sets.newHashSet();
                for (Player p : available) {
                    if (!b.lineups.getAllPlayers().contains(p) && !b.players.contains(p)) {
                        adds.add(new Add(p));
                    }
                }
                return adds;
            }
        };
    }

    private static Callable<Bench> initialStateGenerator(final AllLineups lineups, final Iterable<Player> selected, final TeamStats<BattingStats> predictions, final Iterable<Player> available, final Integer maxSize) {
        return new Callable<Bench>() {
            public Bench call() {
                List<Player> candidates = Lists.newArrayList(
                    Sets.difference(
                        ImmutableSet.copyOf(available),
                        lineups.getAllPlayers()));

                Collections.shuffle(candidates);

                return new Bench(lineups, selected, Iterables.limit(candidates, maxSize - Iterables.size(selected)), maxSize, predictions);
            }
        };
    }

    public static Bench select(AllLineups lineups, Iterable<Player> selected, TeamStats<BattingStats> predictions, Iterable<Player> available, Integer maxSize) {
        HillClimbing.Builder<Bench> builder = HillClimbing
            .<Bench>builder()
            .heuristic(heuristic())
            .validator(validator())
            .actionGenerator(actionGenerator(available));

        return new RepeatedHillClimbing<Bench>(initialStateGenerator(lineups, selected, predictions, available, maxSize), builder).search();
    }

    private static class Add implements Action<Bench> {

        private final Player add;

        public Add(Player add) {
            this.add = add;
        }

        public Bench apply(Bench b) {
            return b.with(add);
        }

    }

    private static class Remove implements Action<Bench> {

        private final Player remove;

        public Remove(Player remove) {
            this.remove = remove;
        }

        public Bench apply(Bench b) {
            return b.without(remove);
        }
    }

}