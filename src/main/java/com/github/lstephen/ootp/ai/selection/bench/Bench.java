package com.github.lstephen.ootp.ai.selection.bench;

import com.github.lstephen.ootp.ai.io.Printable;
import com.github.lstephen.ootp.ai.io.Printables;
import com.github.lstephen.ootp.ai.player.Player;
import com.github.lstephen.ootp.ai.selection.lineup.AllLineups;
import com.github.lstephen.ootp.ai.selection.lineup.Lineup;
import com.github.lstephen.ootp.ai.regression.Predictor;
import com.github.lstephen.ootp.ai.stats.BattingStats;
import com.github.lstephen.ootp.ai.stats.SplitPercentages;
import com.github.lstephen.ootp.ai.stats.TeamStats;

import java.io.PrintWriter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.github.lstephen.ai.search.HillClimbing;
import com.github.lstephen.ai.search.RepeatedHillClimbing;
import com.github.lstephen.ai.search.Validator;
import com.github.lstephen.ai.search.action.Action;
import com.github.lstephen.ai.search.action.ActionGenerator;
import com.github.lstephen.ai.search.action.SequencedAction;

import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;

/**
 *
 * @author lstephen
 */
public class Bench implements Printable {

    private final AllLineups lineups;

    private final ImmutableSet<Player> selected;

    private final ImmutableSet<Player> players;

    private final Integer maxSize;

    private final Predictor predictor;

    private static SplitPercentages pcts;

    private Bench(AllLineups lineups, Iterable<Player> selected, Iterable<Player> players, Integer maxSize, Predictor predictor) {
        this.lineups = lineups;
        this.players = ImmutableSet.copyOf(players);
        this.selected = ImmutableSet.copyOf(selected);
        this.maxSize = maxSize;
        this.predictor = predictor;
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
            predictor);
    }

    private Bench without(Player p) {
        return new Bench(
            lineups,
            selected,
            Iterables.filter(players, Predicates.not(Predicates.equalTo(p))),
            maxSize,
            predictor);
    }

    public ImmutableSet<Player> players() {
        return players;
    }

    private Double score() {
        Double score = 0.0;

        score += (pcts.getVsLhpPercentage() * score(lineups.getVsLhp(), Lineup.VsHand.VS_LHP));
        score += (pcts.getVsLhpPercentage() * score(lineups.getVsLhpPlusDh(), Lineup.VsHand.VS_LHP));
        score += (pcts.getVsRhpPercentage() * score(lineups.getVsRhp(), Lineup.VsHand.VS_RHP));
        score += (pcts.getVsRhpPercentage() * score(lineups.getVsRhpPlusDh(), Lineup.VsHand.VS_RHP));

        return score;
    }

    private Double score(Lineup lineup, Lineup.VsHand vs) {
        return new BenchScorer(predictor)
            .score(
                Iterables.concat(
                    Sets.difference(selected, lineup.playerSet()), players),
                    lineup,
                    vs);
    }

    private Integer totalAge() {
        Integer age = 0;

        for (Player p : players) {
            age += p.getAge();
        }

        return age;
    }

    public void print(PrintWriter w) {
       w.print("Bench:");
       for (Player p : Player.byShortName().sortedCopy(players())) {
           w.print(p.getShortName() + "/");
       }
       w.println();
    }

    private static Validator<Bench> validator() {
        Predicate<Bench> size = (b) ->  b.selected.size() + b.players.size() <= b.maxSize;

        Predicate<Bench> anyInLineup = (b) ->
          b.players
            .stream()
            .filter((p) -> b.lineups.getAllPlayers().contains(p))
            .findAny()
            .isPresent();

        return size.and(anyInLineup.negate())::test;
    }

    private static Ordering<Bench> byScore() {
        return Ordering.natural().onResultOf(Bench::score);
    }

    private static Ordering<Bench> bySize() {
        return Ordering.natural().onResultOf((b) -> b.players.size());
    }

    private static Ordering<Bench> byAge() {
        return Ordering.natural().reverse().onResultOf(Bench::totalAge);
    }

    private static Ordering<Bench> heuristic() {
        return byScore()
            .compound(bySize().reverse())
            .compound(byAge());
    }

    private static ActionGenerator<Bench> actionGenerator(final Iterable<Player> available) {
        Function<Bench, Set<Add>> adds = (b) ->
          StreamSupport.stream(available.spliterator(), false)
            .filter((p) -> !b.lineups.getAllPlayers().contains(p))
            .filter((p) -> !b.players.contains(p))
            .map(Add::new)
            .collect(Collectors.toSet());

        Function<Bench, Set<Remove>> removes = (b) ->
          b.players.stream().map(Remove::new).collect(Collectors.toSet());

        return (b) -> {
          Set<Add> as = adds.apply(b);
          Set<Remove> rs = removes.apply(b);

          return Stream.concat(Stream.concat(as.stream(), rs.stream()), SequencedAction.merged(as, rs));
        };
    }

    private static Supplier<Bench> initialStateGenerator(final AllLineups lineups, final Iterable<Player> selected, final Predictor predictor, final Iterable<Player> available, final Integer maxSize) {
        return new Supplier<Bench>() {
            public Bench get() {
                List<Player> candidates = new ArrayList<>(
                    Sets.difference(
                        ImmutableSet.copyOf(available),
                        lineups.getAllPlayers()));

                Collections.shuffle(candidates);

                return new Bench(lineups, selected, Iterables.limit(candidates, maxSize - Iterables.size(selected)), maxSize, predictor);
            }
        };
    }

    public static Bench select(AllLineups lineups, Iterable<Player> selected, Predictor predictor, Iterable<Player> available, Integer maxSize) {
        HillClimbing<Bench> hc = HillClimbing
            .<Bench>builder()
            .heuristic(heuristic())
            .validator(validator())
            .actionGenerator(actionGenerator(available))
            .build();

       Bench result = new RepeatedHillClimbing<Bench>(initialStateGenerator(lineups, selected, predictor, available, maxSize), hc).search();

       Printables.print(result).to(System.out);

       return result;
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
