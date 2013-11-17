package com.ljs.ootp.ai.selection.lineup;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import com.ljs.ootp.ai.player.Player;
import com.ljs.ootp.ai.player.ratings.DefensiveRatings;
import com.ljs.ootp.ai.player.ratings.Position;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class DefenseSelection {

    public Defense select(Iterable<Player> selected) {
        return Ordering
            .natural()
            .onResultOf(new Function<Defense, Double>() {
                @Override
                public Double apply(Defense def) {
                    return def.score();
                }
            })
            .max(DefenseIterator.create(selected));
    }

    private static class DefenseIterator implements Iterator<Defense> {

	    private final Iterator<List<Player>> permutations;

        private DefenseIterator(Iterator<List<Player>> permutations) {
            this.permutations = permutations;
        }

        @Override
		public boolean hasNext() {
			return permutations.hasNext();
		}

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Defense next() {
            List<Player> permutation = permutations.next();

            Map<Player, Position> defense = Maps.newHashMap();

            int idx = 0;

            for (Position p
                : ImmutableList.of(
                    Position.CATCHER,
                    Position.FIRST_BASE,
                    Position.SECOND_BASE,
                    Position.THIRD_BASE,
                    Position.SHORTSTOP,
                    Position.LEFT_FIELD,
                    Position.CENTER_FIELD,
                    Position.RIGHT_FIELD)) {

                DefensiveRatings ratings = permutation.get(idx).getDefensiveRatings();

                defense.put(permutation.get(idx), p);
                idx++;
            }

            return Defense.create(defense);
        }

        public static DefenseIterator create(Iterable<Player> available) {
            return new DefenseIterator(
                Collections2
                    .permutations(ImmutableSet.copyOf(available))
                    .iterator());
        }
    }
}
