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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DefenseSelection {

    public Map select(Iterable selected) {
        Iterable defenses = getAllDefenses(selected);
        return (Map) Ordering.natural().onResultOf(new Function<Map, Double>() {
            public Double apply(Map def) {
                return DefenseSelection.score(def);
            }
        }).max(defenses);
    }

    private Iterable getAllDefenses(Iterable<Player> selected) {
        Set result = Sets.newHashSet();

        for (List<Player> ps : Collections2.permutations(ImmutableSet.copyOf(
            selected))) {

            Map defense = Maps.newHashMap();
            int idx = 0;
            for (Iterator i$ = ImmutableList.of(Position.CATCHER,
                Position.FIRST_BASE, Position.SECOND_BASE, Position.THIRD_BASE,
                Position.SHORTSTOP, Position.LEFT_FIELD, Position.CENTER_FIELD,
                Position.RIGHT_FIELD).iterator(); i$.hasNext();) {
                Position p = (Position) i$.next();
                DefensiveRatings ratings = ((Player) ps.get(idx))
                    .getDefensiveRatings();
                if (p != Position.FIRST_BASE && ratings.getPositionScore(p)
                    .doubleValue() <= 0.0D) {
                    continue;
                }
                defense.put(ps.get(idx), p);
                idx++;
            }

            result.add(defense);
        }
        return result;
    }

    private static Double score(Map<Player, Position> def) {
        double total = 0.0D;

        for (Map.Entry<Player, Position> pp : def.entrySet()) {
            DefensiveRatings r = ((Player) pp.getKey()).getDefensiveRatings();

            switch (pp.getValue()) {
                case CATCHER:
                case SHORTSTOP:
                    total += 5D * r.getPositionScore((Position) pp.getValue())
                        .doubleValue();
                    break;

                case SECOND_BASE:
                case THIRD_BASE:
                    total += 4D * r.getPositionScore((Position) pp.getValue())
                        .doubleValue();
                    break;

                case CENTER_FIELD:
                    total += 3D * r.getPositionScore((Position) pp.getValue())
                        .doubleValue();
                    break;

                case LEFT_FIELD:
                case RIGHT_FIELD:
                    total += 2D * r.getPositionScore((Position) pp.getValue())
                        .doubleValue();
                    break;

                default:
                    total += r.getPositionScore((Position) pp.getValue())
                        .doubleValue();
                    break;
            }
        }
        return Double.valueOf(total);
    }
}
