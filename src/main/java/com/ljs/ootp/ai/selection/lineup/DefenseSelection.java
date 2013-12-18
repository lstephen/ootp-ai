package com.ljs.ootp.ai.selection.lineup;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.ljs.ai.search.Action;
import com.ljs.ai.search.ActionsFunction;
import com.ljs.ai.search.RepeatedHillClimbing;
import com.ljs.ootp.ai.player.Player;
import com.ljs.ootp.ai.player.ratings.Position;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

public class DefenseSelection {

    public Defense select(Iterable<Player> selected) {
        return new RepeatedHillClimbing<Defense>(
            Defense.class,
            initialStateFunction(selected),
            actionsFunction(selected))
            .search();
    }

    private Callable<Defense> initialStateFunction(final Iterable<Player> selected) {
        return new Callable<Defense>() {
            public Defense call() {
                List<Player> ps = Lists.newArrayList(selected);
                Collections.shuffle(ps);

                Map<Player, Position> defense = Maps.newHashMap();

                List<Position> pos = Lists.newArrayList(
                    Position.CATCHER,
                    Position.FIRST_BASE, Position.SECOND_BASE, Position.THIRD_BASE, Position.SHORTSTOP,
                    Position.LEFT_FIELD, Position.CENTER_FIELD, Position.RIGHT_FIELD);

                while (!pos.isEmpty() && !ps.isEmpty()) {
                    defense.put(ps.remove(0), pos.remove(0));
                }

                return Defense.create(defense);
            }
        };
    }

    private ActionsFunction<Defense> actionsFunction(final Iterable<Player> selected) {
        final Set<Swap> swaps = Sets.newHashSet();

        ImmutableList<Player> ps = ImmutableList.copyOf(selected);

        for (int i = 0; i < ps.size(); i++) {
            for (int j = 0; j < ps.size(); j++) {
                swaps.add(new Swap(ps.get(i), ps.get(j)));
            }
        }

        return new ActionsFunction<Defense>() {
            @Override
            public Iterable<? extends Action<Defense>> getActions(Defense state) {
                return swaps;

            }
        };
    }

    private static class Swap extends Action<Defense> {

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
