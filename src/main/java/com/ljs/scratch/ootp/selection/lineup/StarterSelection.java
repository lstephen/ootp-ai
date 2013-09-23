// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   StarterSelection.java

package com.ljs.scratch.ootp.selection.lineup;

import com.google.common.base.Function;
import com.google.common.collect.*;
import com.ljs.scratch.ootp.core.Player;
import com.ljs.scratch.ootp.ratings.*;
import com.ljs.scratch.ootp.stats.TeamStats;
import java.util.*;

// Referenced classes of package com.ljs.scratch.ootp.selection.lineup:
//            Lineup

public class StarterSelection
{

    public StarterSelection(TeamStats predictions)
    {
        this.predictions = predictions;
    }

    public Iterable selectWithDh(Lineup.VsHand vs, Iterable available)
    {
        Set selected = Sets.newHashSet(select(vs, available));
        selected.add(selectDh(vs, Sets.difference(ImmutableSet.copyOf(available), selected)));
        return selected;
    }

    private Player selectDh(Lineup.VsHand vs, Iterable available)
    {
        for(Iterator i$ = byWoba(vs).sortedCopy(available).iterator(); i$.hasNext();)
        {
            Player p = (Player)i$.next();
            if(containsCatcher(Sets.difference(ImmutableSet.copyOf(available), ImmutableSet.of(p))))
                return p;
        }

        throw new IllegalStateException();
    }

    public Iterable select(Lineup.VsHand vs, Iterable available)
    {
        Set result = Sets.newHashSet();
        Iterator i$ = byWoba(vs).sortedCopy(available).iterator();
        do
        {
            if(!i$.hasNext())
                break;
            Player p = (Player)i$.next();
            Set bench = Sets.newHashSet(Sets.difference(ImmutableSet.copyOf(available), result));
            bench.remove(p);
            if(hasValidDefense(Iterables.concat(result, ImmutableSet.of(p)), bench))
                result.add(p);
        } while(result.size() != 8);
        if(result.size() != 8)
            throw new IllegalStateException();
        else
            return result;
    }

    private boolean hasValidDefense(Iterable selected, Iterable bench)
    {
        if(!containsCatcher(bench))
            return false;
        else
            return hasValidDefense(((Collection) (ImmutableSet.copyOf(selected))), ((Map) (ImmutableMap.of())));
    }

    private boolean containsCatcher(Iterable bench)
    {
        for(Iterator i$ = bench.iterator(); i$.hasNext();)
        {
            Player p = (Player)i$.next();
            if(p.getDefensiveRatings().applyMinimums().getPositionScore(Position.CATCHER).doubleValue() > 0.0D)
                return true;
        }

        return false;
    }

    private boolean hasValidDefense(Collection ps, Map assigned)
    {
        Player p = (Player)Iterables.getFirst(ps, null);
        if(p == null)
            return true;
        DefensiveRatings def = p.getDefensiveRatings().applyMinimums();
        Set nextPlayers = Sets.newHashSet(ps);
        nextPlayers.remove(p);
        Position arr$[] = Position.values();
        int len$ = arr$.length;
        for(int i$ = 0; i$ < len$; i$++)
        {
            Position pos = arr$[i$];
            if(pos != Position.FIRST_BASE && def.getPositionScore(pos).doubleValue() <= 0.0D || assigned.containsKey(pos))
                continue;
            Map nextAssigned = Maps.newHashMap(assigned);
            nextAssigned.put(pos, p);
            if(hasValidDefense(((Collection) (nextPlayers)), nextAssigned))
                return true;
        }

        return false;
    }

    private Ordering byWoba(final Lineup.VsHand vs)
    {
        return Ordering.natural().reverse().onResultOf(new Function<Player, Double>() {

            public Double apply(Player p)
            {
                return Double.valueOf(vs.getStats(predictions, p).getWoba());
            }
        }
).compound(byWeightedRating(vs));
    }

    public static Ordering byWeightedRating(final Lineup.VsHand vs)
    {
        return Ordering.natural().reverse().onResultOf(new Function<Player, Double>() {

            public Double apply(Player p)
            {
                BattingRatings ratings = (BattingRatings)p.getBattingRatings().getVsRight();
                if(vs == Lineup.VsHand.VS_LHP)
                    ratings = (BattingRatings)p.getBattingRatings().getVsLeft();
                return Double.valueOf(0.69999999999999996D * (double)ratings.getEye() + 0.90000000000000002D * (double)ratings.getContact() + 1.3D * (double)ratings.getGap() + 2D * (double)ratings.getPower());
            }
        }
);
    }

    private final TeamStats predictions;

}
