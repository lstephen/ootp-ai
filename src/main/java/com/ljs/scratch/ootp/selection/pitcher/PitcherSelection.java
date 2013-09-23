// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   PitcherSelection.java

package com.ljs.scratch.ootp.selection.pitcher;

import com.google.common.base.Function;
import com.google.common.collect.*;
import com.ljs.scratch.ootp.core.Player;
import com.ljs.scratch.ootp.regression.Predictions;
import com.ljs.scratch.ootp.selection.Slot;
import com.ljs.scratch.ootp.stats.PitcherOverall;
import com.ljs.scratch.ootp.stats.TeamStats;

public class PitcherSelection
{

    public PitcherSelection(TeamStats predicitons, PitcherOverall method)
    {
        predictions = predicitons;
        this.method = method;
    }

    public PitcherSelection(TeamStats predictions, PitcherOverall method, Function value)
    {
        this(predictions, method);
        this.value = value;
    }

    public PitcherOverall getMethod()
    {
        return method;
    }

    public ImmutableMultimap selectMajorLeagueSquad(Iterable available)
    {
        return selectMajorLeagueSquad(((Iterable) (ImmutableSet.of())), available);
    }

    public ImmutableMultimap selectMajorLeagueSquad(Iterable forced, Iterable available)
    {
        return select(forced, available, Slot.MAJOR_LEAGUE_PITCHING_SLOTS, Slot.MAJOR_LEAGUE_PITCHING_SLOTS.size());
    }

    public ImmutableMultimap selectExpandedMajorLeagueSquad(Iterable available)
    {
        return selectExpandedMajorLeagueSquad(((Iterable) (ImmutableSet.of())), available);
    }

    public ImmutableMultimap selectExpandedMajorLeagueSquad(Iterable forced, Iterable available)
    {
        Multiset expanded = HashMultiset.create(Slot.MAJOR_LEAGUE_PITCHING_SLOTS);
        expanded.addAll(ImmutableList.of(Slot.SP, Slot.MR, Slot.P, Slot.P));
        return select(forced, available, expanded, expanded.size());
    }

    public ImmutableMultimap select(Iterable<Player> forced, Iterable<Player> available, Multiset slots, int size)
    {
        Multimap selected = ArrayListMultimap.create();
        Multiset remainingSlots = HashMultiset.create(slots);

        for (Player p : forced) {
            Slot st = Slot.getPrimarySlot(p);
            remainingSlots.remove(st);
            selected.put(st, p);
        }

        for (Player p : byOverall().sortedCopy(available)) {

            for (Slot st : Slot.getPlayerSlots(p)) {
                if(remainingSlots.contains(st) && !selected.containsValue(p) && selected.size() < size)
                {
                    selected.put(st, p);
                    remainingSlots.remove(st);
                }
            }
        }

        return ImmutableMultimap.copyOf(selected);
    }

    public Ordering<Player> byOverall()
    {
        return Ordering.natural().onResultOf(new Function<Player, Double>() {

            public Double apply(Player p)
            {
                if(value == null)
                    return method.get(predictions, p);
                else
                    return (Double)value.apply(p);
            }

        }
).compound(method.byWeightedRating()).compound(Player.byAge());
    }

    public static PitcherSelection using(Predictions predictions)
    {
        return new PitcherSelection(predictions.getAllPitching(), predictions.getPitcherOverall());
    }

    public static PitcherSelection using(Predictions predictions, final Function<Player, ? extends Number> value)
    {
        return new PitcherSelection(predictions.getAllPitching(), predictions.getPitcherOverall(), new Function<Player, Double>() {

            public Double apply(Player p)
            {
                return Double.valueOf(10000D / ((Number)value.apply(p)).doubleValue());
            }
        }
);
    }

    private final TeamStats predictions;
    private final PitcherOverall method;
    private Function value;



}
