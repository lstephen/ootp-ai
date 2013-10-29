package com.ljs.scratch.ootp.selection.lineup;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.ljs.scratch.ootp.player.Player;
import com.ljs.scratch.ootp.ratings.BattingRatings;
import com.ljs.scratch.ootp.ratings.DefensiveRatings;
import com.ljs.scratch.ootp.ratings.Position;
import com.ljs.scratch.ootp.stats.BattingStats;
import com.ljs.scratch.ootp.stats.TeamStats;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import org.fest.assertions.api.Assertions;

// Referenced classes of package com.ljs.scratch.ootp.selection.lineup:
//            Lineup
public class StarterSelection {

    private static final Logger LOG =
        Logger.getLogger(StarterSelection.class.getName());

    private final TeamStats<BattingStats> predictions;

    public StarterSelection(TeamStats<BattingStats> predictions) {
        this.predictions = predictions;
    }

    public Iterable<Player> selectWithDh(
        Lineup.VsHand vs, Iterable<Player> available) {

        Set<Player> selected = Sets.newHashSet(select(vs, available));
        selected.add(
            selectDh(
                vs,
                Sets.difference(ImmutableSet.copyOf(available), selected)));
        return selected;
    }

    private Player selectDh(Lineup.VsHand vs, Iterable<Player> available) {
        for (Player p : byWoba(vs).sortedCopy(available)) {
            if (containsCatcher(
                Sets.difference(ImmutableSet.copyOf(available),
                ImmutableSet.of(p)))) {

                return p;
            }
        }

        throw new IllegalStateException();
    }

    public Iterable<Player> select(
        Lineup.VsHand vs, Iterable<Player> available) {

        Set<Player> result = Sets.newHashSet();

        for (Player p : byWoba(vs).sortedCopy(available)) {
            Set bench =
                Sets.newHashSet(
                    Sets.difference(
                        ImmutableSet.copyOf(available), result));

            bench.remove(p);

            if (hasValidDefense(
                Iterables.concat(result, ImmutableSet.of(p)), bench)) {
                result.add(p);
            }

            if (result.size() == 8) {
                break;
            }
        }

        if (result.size() != 8) {
            LOG.warning("Could not find selection with valid defense");

            while (result.size() < 8) {
                result.add(
                    selectDh(
                        vs,
                        Sets.difference(ImmutableSet.copyOf(available), result)));
            }
        }

        return result;
    }

    private boolean hasValidDefense(Iterable selected, Iterable bench) {
        if (!containsCatcher(bench)) {
            return false;
        } else {
            return hasValidDefense(
                ((Collection) (ImmutableSet.copyOf(selected))),
                ((Map) (ImmutableMap.of())));
        }
    }

    private boolean containsCatcher(Iterable bench) {
        for (Iterator i$ = bench.iterator(); i$.hasNext();) {
            Player p = (Player) i$.next();
            if (p.getDefensiveRatings().applyMinimums().getPositionScore(
                Position.CATCHER).doubleValue() > 0.0D) {
                return true;
            }
        }

        return false;
    }

    private boolean hasValidDefense(Collection ps, Map assigned) {
        Player p = (Player) Iterables.getFirst(ps, null);
        if (p == null) {
            return true;
        }
        DefensiveRatings def = p.getDefensiveRatings().applyMinimums();
        Set nextPlayers = Sets.newHashSet(ps);
        nextPlayers.remove(p);
        Position arr$[] = Position.values();
        int len$ = arr$.length;
        for (int i$ = 0; i$ < len$; i$++) {
            Position pos = arr$[i$];
            if (pos != Position.FIRST_BASE && def.getPositionScore(pos)
                .doubleValue() <= 0.0D || assigned.containsKey(pos)) {
                continue;
            }
            Map nextAssigned = Maps.newHashMap(assigned);
            nextAssigned.put(pos, p);
            if (hasValidDefense(((Collection) (nextPlayers)), nextAssigned)) {
                return true;
            }
        }

        return false;
    }

    private Ordering<Player> byWoba(final Lineup.VsHand vs) {
        return Ordering.natural().reverse().onResultOf(
            new Function<Player, Double>() {
            public Double apply(Player p) {
                return Double.valueOf(vs.getStats(predictions, p).getWoba());
            }
        }).compound(byWeightedRating(vs));
    }

    public static Ordering byWeightedRating(final Lineup.VsHand vs) {
        return Ordering.natural().reverse().onResultOf(
            new Function<Player, Double>() {
            public Double apply(Player p) {
                Assertions.assertThat(p).isNotNull();

                BattingRatings ratings = p.getBattingRatings().getVsRight();

                if (vs == Lineup.VsHand.VS_LHP) {
                    ratings = (BattingRatings) p.getBattingRatings().getVsLeft();
                }

                return Double.valueOf(0.7 * ratings.getEye()
                    + 0.9 * ratings.getContact()
                    + 1.3 * ratings.getGap()
                    + 2.0 * ratings.getPower());
            }
        });
    }

}
