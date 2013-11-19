package com.ljs.ootp.ai.selection;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multiset;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.ljs.ootp.ai.player.Player;
import com.ljs.ootp.ai.player.Slot;
import com.ljs.ootp.ai.selection.lineup.AllLineups;
import com.ljs.ootp.ai.selection.lineup.LineupSelection;
import com.ljs.ootp.ai.stats.BattingStats;
import com.ljs.ootp.ai.stats.SplitPercentages;
import com.ljs.ootp.ai.stats.TeamStats;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author lstephen
 */
public class PrioritzeStarterSelection implements Selection {

    private final Multiset<Slot> slots;

    private final Function<Player, Integer> value;

    private final TeamStats<BattingStats> predictions;

    private final SplitPercentages splits;

    private Set<Player> didntMakeLineup = Sets.newHashSet();

    public PrioritzeStarterSelection(Multiset<Slot> slots, Function<Player, Integer> value, TeamStats<BattingStats> predictions, SplitPercentages splits) {
        this.slots = slots;
        this.value = value;
        this.predictions = predictions;
        this.splits = splits;
    }

    @Override
    public ImmutableMultimap<Slot, Player> select(
        Iterable<Player> forced,
        Iterable<Player> available) {

        Multimap<Slot, Player> selected = ArrayListMultimap.create();
        Multiset<Slot> remainingSlots = HashMultiset.create(slots);

        for (Player p : Selections.onlyHitters(forced)) {
            for (Slot s : Slot.getPlayerSlots(p)) {
                if (remainingSlots.contains(s)) {
                    selected.put(s, p);
                    remainingSlots.remove(s);
                    break;
                }
            }
        }

        Set<Player> remainingPlayers = Selections.onlyHitters(Sets.difference(ImmutableSet.copyOf(available), ImmutableSet.copyOf(forced)));

        return select(selected, remainingPlayers, remainingSlots);
    }


    private ImmutableMultimap<Slot, Player> select(
        Multimap<Slot, Player> selected, Set<Player> available, Multiset<Slot> remaining) {

        didntMakeLineup = Sets.newHashSet();

        return new Selector(selected, available, remaining).call();
    }

    private Ordering<Player> byPriority(final Multimap<Slot, Player> selected) {
        final LineupSelection lineup = new LineupSelection(predictions);

        final Map<Player, Integer> cache = Maps.newHashMap();

        return Ordering
            .natural()
            .reverse()
            .onResultOf(new Function<Player, Integer>() {
                public Integer apply(Player p) {
                    if (cache.containsKey(p)) {
                        return cache.get(p);
                    }

                    if (selected.size() < 9) {
                        return value.apply(p) * 3;
                    }
                    if (didntMakeLineup.contains(p)) {
                        System.out.println("Didn't make lineup:" + p.getShortName());
                        return value.apply(p);
                    }

                    System.out.println("Lineup for:" + p.getShortName());

                    AllLineups lineups = lineup.select(Iterables.concat(selected.values(), ImmutableSet.of(p)));

                    if (!Iterables.contains(lineups.getAllPlayers(), p)) {
                        didntMakeLineup.add(p);
                        cache.put(p, value.apply(p));
                        return value.apply(p);
                    } else {
                        Double score = (double) value.apply(p);

                        if (lineups.getVsRhp().playerSet().contains(p)) {
                            System.out.println("RHP");
                            score += splits.getVsRhpPercentage() * predictions.getSplits(p).getVsRight().getWobaPlus();
                        }
                        if (lineups.getVsRhpPlusDh().playerSet().contains(p)) {
                            System.out.println("RHP+DH");
                            score += splits.getVsRhpPercentage() * predictions.getSplits(p).getVsRight().getWobaPlus();
                        }
                        if (lineups.getVsLhp().playerSet().contains(p)) {
                            System.out.println("LHP");
                            score += splits.getVsLhpPercentage() * predictions.getSplits(p).getVsLeft().getWobaPlus();
                        }
                        if (lineups.getVsLhpPlusDh().playerSet().contains(p)) {
                            System.out.println("LHP+DH");
                            score += splits.getVsLhpPercentage() * predictions.getSplits(p).getVsLeft().getWobaPlus();
                        }

                        cache.put(p, score.intValue());

                        System.out.println("Score:" + score.intValue());
                        return score.intValue();
                    }
                }
            })
            .compound(Player.byTieBreak());
    }

    private class Selector {
        private Multimap<Slot, Player> selected;
        private Set<Player> available;
        private Multiset<Slot> remaining;


        public Selector(Multimap<Slot, Player> selected, Set<Player> available, Multiset<Slot> remaining) {
            this.selected = ArrayListMultimap.create(selected);
            this.available = Sets.newHashSet(available);
            this.remaining = HashMultiset.create(remaining);
        }

        public ImmutableMultimap<Slot, Player> call() {
            if (remaining.isEmpty()) {
                return ImmutableMultimap.copyOf(selected);
            }

            System.out.println("Remaining:" + Joiner.on(',').join(remaining));

            Iterable<Player> eligible = Iterables.filter(
                available,
                new Predicate<Player>() {

                    public boolean apply(Player p) {
                        for (Slot s : p.getSlots()) {
                            if (remaining.contains(s)) {
                                return true;
                            }
                        }
                        return false;
                    }
                });

            List<Player> ps = byPriority(selected).sortedCopy(eligible);

            System.out.println("Eligible Top 5");

            for (int i = 0; i < 5; i++) {
                System.out.println(ps.get(i).getShortName());
            }

            for (Player p : byPriority(selected).sortedCopy(eligible)) {
                for (Slot s : p.getSlots()) {
                    if (remaining.contains(s)) {
                        selected.put(s, p);
                        available.remove(p);
                        remaining.remove(s);
                        System.out.println(String.format("Selected: %s (%s)", p.getShortName(), s));
                        return call();
                    }
                }
            }

            throw new IllegalStateException();
        }

    }

}
