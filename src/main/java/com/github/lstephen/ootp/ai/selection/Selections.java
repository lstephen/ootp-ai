package com.github.lstephen.ootp.ai.selection;

import com.github.lstephen.ootp.ai.player.Player;
import com.github.lstephen.ootp.ai.player.Slot;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

// Referenced classes of package com.ljs.scratch.ootp.selection:
//            Selection

public final class Selections {

    private Selections() { }

    public static ImmutableMultimap<Slot, Player> select(Selection selection, Iterable available) {
        return selection.select(ImmutableSet.<Player>of(), available);
    }

    public static ImmutableSet<Player> onlyHitters(Iterable<Player> ps) {
        return ImmutableSet.copyOf(Iterables.filter(ps, Selections::isHitter));
    }

    public static ImmutableSet<Player> onlyPitchers(Iterable<Player> ps) {
        return ImmutableSet.copyOf(Iterables.filter(ps, Selections::isPitcher));
    }

    public static boolean isHitter(Player p) {
        return p.isHitter();
    }

    public static boolean isPitcher(Player p) {
        return p.isPitcher();
    }

    public static ImmutableSet<Player> onlyOn40Man(Iterable<Player> ps) {
        return ImmutableSet.copyOf(Iterables.filter(ps, p -> p.getOn40Man().or(Boolean.TRUE)));
    }

}
