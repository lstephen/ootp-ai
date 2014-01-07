package com.ljs.ootp.ai.selection.bench;

import com.google.common.base.Function;
import com.google.common.base.Optional;
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

    private final ImmutableSet<Player> players;

    private final Integer maxSize;

    private final TeamStats<BattingStats> predictions;

    private static SplitPercentages pcts;

    private Bench(AllLineups lineups, Iterable<Player> players, Integer maxSize, TeamStats<BattingStats> predictions) {
        this.lineups = lineups;
        this.players = ImmutableSet.copyOf(players);
        this.maxSize = maxSize;
        this.predictions = predictions;
    }

    public static void setPercentages(SplitPercentages pcts) {
        Bench.pcts = pcts;
    }

    private Bench with(Player p) {
        return new Bench(
            lineups,
            Iterables.concat(players, ImmutableSet.of(p)),
            maxSize,
            predictions);
    }

    private Bench without(Player p) {
        return new Bench(
            lineups,
            Iterables.filter(players, Predicates.not(Predicates.equalTo(p))),
            maxSize,
            predictions);
    }

    public ImmutableSet<Player> players() {
        return players;
    }

    private Integer score() {
        Integer score = 0;

        for (Player p : players()) {
            score += predictions.getOverall(p).getWobaPlus();
        }

        score += (int) (pcts.getVsLhpPercentage() * score(lineups.getVsLhp(), Lineup.VsHand.VS_LHP));
        score += (int) (pcts.getVsLhpPercentage() * score(lineups.getVsLhpPlusDh(), Lineup.VsHand.VS_LHP));
        score += (int) (pcts.getVsRhpPercentage() * score(lineups.getVsRhp(), Lineup.VsHand.VS_RHP));
        score += (int) (pcts.getVsRhpPercentage() * score(lineups.getVsRhpPlusDh(), Lineup.VsHand.VS_RHP));

        return score;
    }

    private Integer score(Lineup lineup, Lineup.VsHand vs) {

        Integer score = 0;

        for (Lineup.Entry entry : lineup) {
            if (!entry.getPositionEnum().equals(Position.PITCHER)) {
                Optional<Player> p = selectBenchPlayer(vs, entry.getPositionEnum());

                if (p.isPresent()) {
                    score += vs.getStats(predictions, p.get()).getWobaPlus();
                }
            }
        }

        return score;
    }

    private Optional<Player> selectBenchPlayer(final Lineup.VsHand vs, Position pos) {
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

        for (Player p : sortedBench) {
            if (p.canPlay(pos)) {
                return Optional.of(p);
            }
        }

        return Optional.absent();
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
                if (b.lineups.getAllPlayers().size() + b.players.size() > b.maxSize) {
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
            .onResultOf(new Function<Bench, Integer>() {
                public Integer apply(Bench b) {
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
                System.out.print("sc:" + b.score() + " ");
                for (Player p : b.players) {
                    System.out.print(p.getShortName() + "/");
                }
                System.out.println();
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

    private static Callable<Bench> initialStateGenerator(final AllLineups lineups, final TeamStats<BattingStats> predictions, final Iterable<Player> available, final Integer maxSize) {
        return new Callable<Bench>() {
            public Bench call() {
                List<Player> candidates = Lists.newArrayList(
                    Sets.difference(
                        ImmutableSet.copyOf(available),
                        lineups.getAllPlayers()));

                Collections.shuffle(candidates);

                return new Bench(lineups, Iterables.limit(candidates, maxSize - lineups.getAllPlayers().size()), maxSize, predictions);
            }
        };
    }

    public static Bench select(AllLineups lineups, TeamStats<BattingStats> predictions, Iterable<Player> available, Integer maxSize) {
        HillClimbing.Builder<Bench> builder = HillClimbing
            .<Bench>builder()
            .heurisitic(heuristic())
            .validator(validator())
            .actionGenerator(actionGenerator(available));

        return new RepeatedHillClimbing<Bench>(initialStateGenerator(lineups, predictions, available, maxSize), builder).search();
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
