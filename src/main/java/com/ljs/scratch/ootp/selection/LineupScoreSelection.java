package com.ljs.scratch.ootp.selection;

import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Throwables;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.ljs.scratch.ootp.player.Player;
import com.ljs.scratch.ootp.ratings.Position;
import com.ljs.scratch.ootp.regression.Predictions;
import com.ljs.scratch.ootp.selection.lineup.AllLineups;
import com.ljs.scratch.ootp.selection.lineup.Lineup;
import com.ljs.scratch.ootp.selection.lineup.LineupSelection;
import com.ljs.scratch.ootp.regression.SplitPercentages;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import org.paukov.combinatorics.Factory;
import org.paukov.combinatorics.Generator;
import org.paukov.combinatorics.ICombinatoricsVector;

/**
 *
 * @author lstephen
 */
public class LineupScoreSelection implements Selection {

    private static final Integer NO_DEFENSE_PENALTY = 100000;

    private final SplitPercentages splitPercentages;

    private final Predictions predictions;

    private final OutputStream out;

    private Integer slots = 11;

    private Iterable<Slot> bench = ImmutableList.of(Slot.C, Slot.IF, Slot.OF);

    public LineupScoreSelection(SplitPercentages splitPercentages, Predictions predictions, OutputStream out) {
        this.splitPercentages = splitPercentages;
        this.predictions = predictions;
        this.out = out;
    }

    @Override
    public ImmutableMultimap<Slot, Player> select(
        Iterable<Player> forced, Iterable<Player> available) {

        Set<Player> all = ImmutableSet.copyOf(Iterables.concat(forced, available));

        final LineupSelection lineupSelection =
            new LineupSelection(predictions.getAllBatting());

        AllLineups selection = lineupSelection.select(all);

        AllLineups selected = selection;

        if (Iterables.size(selected.getAllPlayers()) > slots) {
            writePlayers(out, "Common", selection.getCommonPlayers());
            writePlayers(out, "Others", Sets.difference(
                ImmutableSet.copyOf(selection.getAllPlayers()),
                ImmutableSet.copyOf(selection.getCommonPlayers())));

            Iterable<Set<Player>> allPossible =
                getAllPossible(
                Iterables.concat(forced, selection.getCommonPlayers()),
                selection.getAllPlayers());

            final Integer size = Iterables.size(allPossible);

            final AtomicInteger count = new AtomicInteger(0);

            Iterable<Optional<AllLineups>> allLineups =
                Iterables.transform(
                allPossible,
                new Function<Set<Player>, Optional<AllLineups>>() {
                    public Optional<AllLineups> apply(final Set<Player> ps) {
                        Integer current = count.getAndIncrement();
                        try {
                            System.out.println(String.format("%d/%d", current, size));
                            return Optional.of(lineupSelection.select(ps));
                        } catch (IllegalStateException e) {
                            return Optional.absent();
                        }
                    }});


            //System.out.println("Found lineups:" + Iterables.size(allLineups));
            System.out.println("*HERE*");

            final AtomicInteger max = new AtomicInteger(Integer.MIN_VALUE);

            selected = Ordering
                .natural()
                .onResultOf(new Function<Optional<AllLineups>, Integer>() {
                    public Integer apply(Optional<AllLineups> lineups) {
                        Integer score = score(lineups);

                        if (score > max.get()) {
                            try {
                                out.write(String.format("%n---%n%d%n---%n", score).getBytes(Charsets.UTF_8));
                                lineups.get().print(out);
                                out.flush();
                            } catch (IOException e) {
                                throw Throwables.propagate(e);
                            }
                            max.set(score);
                        }

                        return score;
                    }
                })
                .max(allLineups)
                .get();
        }

         Multimap<Slot, Player> result = HashMultimap.create();

         for (Player p : selected.getAllPlayers()) {
             result.put(Slot.getPrimarySlot(p), p);
             try {
                 out.write(String.format("%s%n", p.getShortName()).getBytes(Charsets.UTF_8));
             } catch (IOException e) {

             }
         }

         selected.print(out);

         return ImmutableMultimap.copyOf(result);
    }

    private void writePlayers(OutputStream out, String title, Iterable<Player> ps) {
        PrintWriter w = new PrintWriter(out);

        w.println();
        w.println("---");
        w.println(title);
        w.println("---");

        for (Player p : ps) {
            w.println(String.format("%s %s", p.getPosition(), p.getShortName()));
        }

        w.flush();


    }

    private Iterable<Set<Player>> getAllPossible(
        Iterable<Player> forced, Iterable<Player> available) {

        Set<Player> reallyAvailable =
            Sets.difference(
                ImmutableSet.copyOf(available), ImmutableSet.copyOf(forced));

        ICombinatoricsVector<Player> vector = Factory.createVector(reallyAvailable);

        Generator<Player> gen = Factory.createSimpleCombinationGenerator(vector, slots - Iterables.size(forced));

        Set<Set<Player>> result = Sets.newHashSet();

        for (ICombinatoricsVector<Player> ps : gen) {
            result.add(ImmutableSet.copyOf(Iterables.concat(forced, Sets.newHashSet(ps))));
        }

        return result;
    }



    private Integer score(Optional<AllLineups> lineupsO) {
        if (!lineupsO.isPresent()) {
            return Integer.MIN_VALUE;
        }

        AllLineups lineups = lineupsO.get();

        Integer vsRhp = score(lineups.getVsRhp(), Lineup.VsHand.VS_RHP);
        Integer vsRhpDh = score(lineups.getVsRhpPlusDh(), Lineup.VsHand.VS_RHP);
        Integer vsLhp = score(lineups.getVsLhp(), Lineup.VsHand.VS_LHP);
        Integer vsLhpDh = score(lineups.getVsLhpPlusDh(), Lineup.VsHand.VS_LHP);

        System.out.println(String.format("%.3f/%.3f", splitPercentages.getVsLhpPercentage(), splitPercentages.getVsRhpPercentage()));

        System.out.println(String.format("%d/%d/%d/%d", vsRhp, vsRhpDh, vsLhp, vsLhpDh));

        Double dhScore =
            splitPercentages.getVsRhpPercentage() * vsRhpDh
            + splitPercentages.getVsLhpPercentage() * vsLhpDh;

        Double nonDhScore =
            splitPercentages.getVsRhpPercentage() * vsRhp
            + splitPercentages.getVsLhpPercentage() * vsLhp;

        return (int) (dhScore + nonDhScore);
    }

    private Integer score(Lineup lineup, Lineup.VsHand hand) {
        Integer score = 0;

        for (Lineup.Entry entry : lineup) {
            if (entry.getPlayer() != null) {
                score += hand.getStats(predictions.getAllBatting(), entry.getPlayer()).getWobaPlus();

                if (entry.getPositionEnum() != Position.DESIGNATED_HITTER && entry.getPositionEnum() != Position.FIRST_BASE && entry.getPlayer().getDefensiveRatings().getPositionScore(entry.getPositionEnum()) == 0) {
                    score -= NO_DEFENSE_PENALTY;
                };
            }
        }

        return score;
    }

}
