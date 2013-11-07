// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   LineupOrdering.java

package com.ljs.scratch.ootp.selection.lineup;

import com.google.common.base.Function;
import com.google.common.collect.*;
import com.ljs.scratch.ootp.player.Player;
import com.ljs.scratch.ootp.player.ratings.BattingRatings;
import com.ljs.scratch.ootp.stats.TeamStats;
import java.util.Set;

// Referenced classes of package com.ljs.scratch.ootp.selection.lineup:
//            StarterSelection, Lineup

public class LineupOrdering
{

    public LineupOrdering(TeamStats predictions)
    {
        this.predictions = predictions;
    }

    public ImmutableList order(Lineup.VsHand vs, Iterable ps)
    {
        ImmutableList topFive = orderTopFive(vs, byWoba(vs).reverse().sortedCopy(ps).subList(0, 5));
        Iterable rest = byWoba(vs).reverse().sortedCopy(Sets.difference(ImmutableSet.copyOf(ps), ImmutableSet.copyOf(topFive)));
        return ImmutableList.copyOf(Iterables.concat(topFive, rest));
    }

    private ImmutableList orderTopFive(Lineup.VsHand vs, Iterable ps)
    {
        Set remaining = Sets.newHashSet(ps);
        Player fourth = (Player)byWoba(vs).max(remaining);
        remaining.remove(fourth);
        Player third = (Player)bySlg(vs).max(remaining);
        remaining.remove(third);
        Player fifth = (Player)byObp(vs).min(remaining);
        remaining.remove(fifth);
        Player second = (Player)bySlg(vs).max(remaining);
        remaining.remove(second);
        Player first = (Player)Iterables.getOnlyElement(remaining);
        return ImmutableList.of(first, second, third, fourth, fifth);
    }

    private Ordering byWoba(final Lineup.VsHand vs)
    {
        return Ordering.natural().onResultOf(new Function<Player, Double>() {

            public Double apply(Player p)
            {
                return Double.valueOf(vs.getStats(predictions, p).getWoba());
            }

        }
).compound(StarterSelection.byWeightedRating(vs).reverse());
    }

    private Ordering bySlg(final Lineup.VsHand vs)
    {
        return Ordering.natural().onResultOf(new Function<Player, Double>() {

            public Double apply(Player p)
            {
                return Double.valueOf(vs.getStats(predictions, p).getSluggingPercentage());
            }
        }
).compound(Ordering.natural().onResultOf(new Function<Player, Integer>() {

            public Integer apply(Player p)
            {
                BattingRatings r = vs.getRatings(p);
                return Integer.valueOf(r.getContact() + r.getGap() + 3 * r.getPower());
            }
        }
));
    }

    private Ordering byObp(final Lineup.VsHand vs)
    {
        return Ordering.natural().onResultOf(new Function<Player, Double>() {

            public Double apply(Player p)
            {
                return Double.valueOf(vs.getStats(predictions, p).getOnBasePercentage());
            }
        }
).compound(Ordering.natural().onResultOf(new Function<Player, Integer>() {

            public Integer apply(Player p)
            {
                BattingRatings r = vs.getRatings(p);
                return Integer.valueOf(r.getContact() + r.getEye());
            }
        }
));
    }

    private final TeamStats predictions;

}
