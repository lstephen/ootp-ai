package com.ljs.scratch.ootp.selection.lineup;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.ljs.scratch.ootp.core.Player;
import com.ljs.scratch.ootp.ratings.DefensiveRatings;
import com.ljs.scratch.ootp.ratings.Position;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DefenseSelection {

    public Defense select(Iterable<Player> selected) {
        Iterable<Defense> defenses = getAllDefenses(selected);
        return Ordering
            .natural()
            .onResultOf(new Function<Defense, Double>() {
                @Override
                public Double apply(Defense def) {
                    return def.score();
                }
            })
            .max(defenses);
    }

    private Iterable<Defense> getAllDefenses(Iterable<Player> selected) {
        Set<Defense> result = Sets.newHashSet();

        for (List<Player> ps
            : Collections2.permutations(ImmutableSet.copyOf(selected))) {

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

                DefensiveRatings ratings = ps.get(idx).getDefensiveRatings();

                if (p != Position.FIRST_BASE
                    && ratings.getPositionScore(p) <= 0) {
                    continue;
                }
                defense.put(ps.get(idx), p);
                idx++;
            }

            result.add(Defense.create(defense));
        }
        return result;
    }
}
