package com.ljs.scratch.ootp.selection;

import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.ljs.scratch.ootp.core.Player;
import com.ljs.scratch.ootp.regression.Predictions;
import com.ljs.scratch.ootp.selection.lineup.AllLineups;
import com.ljs.scratch.ootp.selection.lineup.Lineup;
import com.ljs.scratch.ootp.selection.lineup.LineupSelection;
import com.ljs.scratch.ootp.stats.SplitPercentages;
import java.io.IOException;
import java.io.OutputStream;
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

    private static final Double DH_NON_DH_FACTOR = 0.1;

    private static final Integer NO_DEFENSE_PENALTY = 100000;

    private boolean dh = false;

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

        final AtomicInteger count = new AtomicInteger(0);

        Iterable<Set<Player>> allPossible = getAllPossible(forced, selection.getAllPlayers());

        final Integer size = Iterables.size(allPossible);

        Iterable<AllLineups> allLineups = Optional.presentInstances(
            Iterables.transform(
                allPossible,
                new Function<Set<Player>, Optional<AllLineups>>() {
                    public Optional<AllLineups> apply(Set<Player> ps) {
                        Integer current = count.incrementAndGet();

                        System.out.println(String.format("%d/%d", current, size));
                        try {
                            return Optional.of(lineupSelection.select(ps));
                        } catch (IllegalStateException e) {
                            return Optional.absent();
                        }
                    }
                }));

         AllLineups selected = Ordering
            .natural()
            .onResultOf(new Function<AllLineups, Integer>() {
                public Integer apply(AllLineups lineups) {
                    return score(lineups);
                }
            })
            .max(allLineups);

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

    private Iterable<Set<Player>> getAllPossible(
        Iterable<Player> forced, Iterable<Player> available) {

        Set<Player> reallyAvailable =
            Sets.difference(
                ImmutableSet.copyOf(available), ImmutableSet.copyOf(forced));

        ICombinatoricsVector<Player> vector = Factory.createVector(reallyAvailable);

        Generator<Player> gen = Factory.createSimpleCombinationGenerator(vector, slots - Iterables.size(forced));

        Set<Set<Player>> result = Sets.newHashSet();

        for (ICombinatoricsVector<Player> ps : gen) {
            result.add(Sets.newHashSet(ps));
        }

        return result;
    }



    private Integer score(AllLineups lineups) {
        Double dhScore =
            (splitPercentages.getVsRhpPercentage() * score(lineups.getVsRhpPlusDh(), Lineup.VsHand.VS_RHP)
            + splitPercentages.getVsLhpPercentage() * score(lineups.getVsLhpPlusDh(), Lineup.VsHand.VS_LHP));

        Double nonDhScore =
            splitPercentages.getVsRhpPercentage() * score(lineups.getVsRhp(), Lineup.VsHand.VS_RHP)
            + splitPercentages.getVsLhpPercentage() * score(lineups.getVsLhp(), Lineup.VsHand.VS_LHP);

        return (int) (dh
            ? dhScore + DH_NON_DH_FACTOR * nonDhScore
            : nonDhScore + DH_NON_DH_FACTOR * dhScore);
    }

    private Integer score(Lineup lineup, Lineup.VsHand hand) {
        Integer score = 0;

        for (Lineup.Entry entry : lineup) {
            if (entry.getPlayer() != null) {
                score += hand.getStats(predictions.getAllBatting(), entry.getPlayer()).getWobaPlus();

                if (entry.getPlayer().getDefensiveRatings().getPositionScore(entry.getPositionEnum()) == 0) {
                    score -= NO_DEFENSE_PENALTY;
                };
            }
        }

        return score;
    }

}
