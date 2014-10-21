package com.ljs.ootp.ai.selection.lineup;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.ljs.ai.search.hillclimbing.HillClimbing;
import com.ljs.ai.search.hillclimbing.RepeatedHillClimbing;
import com.ljs.ai.search.hillclimbing.action.Action;
import com.ljs.ai.search.hillclimbing.action.ActionGenerator;
import com.ljs.ai.search.hillclimbing.action.SequencedAction;
import com.ljs.ootp.ai.player.Player;
import java.util.Map;
import java.util.Set;

public class DefenseSelection {

    private static final Map<ImmutableSet<Player>, Defense> CACHE = Maps.newHashMap();

    public Defense select(Iterable<Player> selected) {
        return select(ImmutableSet.copyOf(selected));
    }

    private Defense select(ImmutableSet<Player> selected) {
        if (!CACHE.containsKey(selected)) {
            HillClimbing.Builder<Defense> builder = HillClimbing
                .<Defense>builder()
                .actionGenerator(actionsFunction(selected))
                .heuristic(heuristic());

            CACHE.put(
                selected,
                new RepeatedHillClimbing<Defense>(
                    Defense.randomGenerator(selected),
                    builder)
                .search());
        }
        return CACHE.get(selected);
    }

    private Ordering<Defense> heuristic() {
        return Defense.byScore()
            .compound(Defense.byRawRange())
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

        return new ActionGenerator<Defense>() {
            @Override
            public Iterable<Action<Defense>> apply(Defense state) {
                return Iterables.concat(swaps, SequencedAction.allPairs(swaps));
            }
        };
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
