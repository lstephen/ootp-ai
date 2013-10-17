// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   Selections.java

package com.ljs.scratch.ootp.selection;

import com.google.common.base.Predicate;
import com.google.common.collect.*;
import com.ljs.scratch.ootp.core.*;
import com.ljs.scratch.ootp.team.Team;
import java.util.Iterator;
import java.util.Set;

// Referenced classes of package com.ljs.scratch.ootp.selection:
//            Selection

public final class Selections {

    private Selections() { }

    public static ImmutableMultimap<Slot, Player> select(Selection selection, Iterable available)
    {
        return selection.select(ImmutableSet.<Player>of(), available);
    }

    public static ImmutableSet onlyHitters(Team t, Iterable ids)
    {
        Set players = Sets.newHashSet();
        PlayerId id;
        for(Iterator i$ = ids.iterator(); i$.hasNext(); players.add(t.getPlayer(id)))
            id = (PlayerId)i$.next();

        return onlyHitters(((Iterable) (players)));
    }

    public static ImmutableSet<Player> onlyHitters(Iterable<Player> ps) {
        return ImmutableSet.copyOf(Iterables.filter(ps, IS_HITTER));
    }

    public static ImmutableSet onlyPitchers(Team t, Iterable ids)
    {
        Set players = Sets.newHashSet();
        PlayerId id;
        for(Iterator i$ = ids.iterator(); i$.hasNext(); players.add(t.getPlayer(id)))
            id = (PlayerId)i$.next();

        return onlyPitchers(((Iterable) (players)));
    }

    public static ImmutableSet<Player> onlyPitchers(Iterable<Player> ps) {
        return ImmutableSet.copyOf(Iterables.filter(ps, IS_PITCHER));
    }

    public static boolean isHitter(Player p)
    {
        return p.getBattingRatings() != null && !isPitcher(p);
    }

    public static boolean isPitcher(Player p)
    {
        return p.hasPitchingRatings();
    }

    public static ImmutableSet<Player> onlyOn40Man(Iterable<Player> ps) {
        return ImmutableSet.copyOf(Iterables.filter(ps, IS_ON_40_MAN));
    }

    private static final Predicate<Player> IS_HITTER =
        new Predicate<Player>() {
            @Override
            public boolean apply(Player p) {
                return Selections.isHitter(p);
            }};

    private static final Predicate<Player> IS_PITCHER =
        new Predicate<Player>() {
            public boolean apply(Player p) {
                return Selections.isPitcher(p);
            }};

    private static final Predicate<Player> IS_ON_40_MAN =
        new Predicate<Player>() {
            @Override
            public boolean apply(Player p) {
                return p.getOn40Man().or(Boolean.TRUE);
            }};

}
