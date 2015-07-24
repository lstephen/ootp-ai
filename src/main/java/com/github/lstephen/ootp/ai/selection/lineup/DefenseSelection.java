package com.github.lstephen.ootp.ai.selection.lineup;

import com.github.lstephen.ootp.ai.player.Player;

import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import com.github.lstephen.ai.search.HillClimbing;
import com.github.lstephen.ai.search.RepeatedHillClimbing;
import com.github.lstephen.ai.search.action.Action;
import com.github.lstephen.ai.search.action.ActionGenerator;
import com.github.lstephen.ai.search.action.SequencedAction;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;

public class DefenseSelection {

    private static final Map<ImmutableSet<Player>, Defense> CACHE = Maps.newHashMap();

    public Defense select(Iterable<Player> selected) {
        return select(ImmutableSet.copyOf(selected));
    }

    private Defense select(ImmutableSet<Player> selected) {
        if (!CACHE.containsKey(selected)) {
            HillClimbing<Defense> hc = HillClimbing
                .<Defense>builder()
                .actionGenerator(actionsFunction(selected))
                .heuristic(heuristic())
                .build();

            CACHE.put(
                selected,
                new RepeatedHillClimbing<Defense>(
                    Defense.randomGenerator(selected),
                    hc)
                .search());
        }
        return CACHE.get(selected);
    }

    private Ordering<Defense> heuristic() {
        return Defense.byScore()
            .compound(Defense.byBestPositionRating())
            .compound(Defense.byAge().reverse());
    }

    private ActionGenerator<Defense> actionsFunction(final Iterable<Player> selected) {
        final Set<Swap> swaps = Sets.newHashSet();

        ImmutableList<Player> ps = ImmutableList.copyOf(selected);

        for (int i = 0; i < ps.size(); i++) {
            for (int j = 0; j < ps.size(); j++) {
                swaps.add(new Swap(ps.get(i), ps.get(j)));
            }
        }

        return (state) -> Stream.concat(swaps.stream(), SequencedAction.allPairs(swaps));
    }

    private static class Swap implements Action<Defense> {

        private final Player lhs;
        private final Player rhs;

        public Swap(Player lhs, Player rhs) {
            this.lhs = lhs;
            this.rhs = rhs;
        }

        public Defense apply(Defense d) {
            return d.swap(lhs, rhs);
        }
    }

}
