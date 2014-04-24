package com.ljs.ootp.ai.selection;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.ljs.ootp.ai.player.Player;
import com.ljs.ootp.ai.player.Slot;
import org.fest.assertions.api.Assertions;

// Referenced classes of package com.ljs.scratch.ootp.selection:
//            Selection

public final class Selections {

    private Selections() { }

    public static ImmutableMultimap<Slot, Player> select(Selection selection, Iterable available) {
        return selection.select(ImmutableSet.<Player>of(), available);
    }

    public static ImmutableSet<Player> onlyHitters(Iterable<Player> ps) {
        return ImmutableSet.copyOf(Iterables.filter(ps, IS_HITTER));
    }

    public static ImmutableSet<Player> onlyPitchers(Iterable<Player> ps) {
        return ImmutableSet.copyOf(Iterables.filter(ps, IS_PITCHER));
    }

    public static boolean isHitter(Player p) {
        return p.isHitter();
    }

    public static boolean isPitcher(Player p) {
        return p.isPitcher();
    }

    public static ImmutableSet<Player> onlyOn40Man(Iterable<Player> ps) {
        return ImmutableSet.copyOf(Iterables.filter(ps, IS_ON_40_MAN));
    }

    private static final Predicate<Player> IS_HITTER =
        new Predicate<Player>() {
            @Override
            public boolean apply(Player p) {
                Assertions.assertThat(p).isNotNull();
                return Selections.isHitter(p);
            }};

    private static final Predicate<Player> IS_PITCHER =
        new Predicate<Player>() {
            @Override
            public boolean apply(Player p) {
                Assertions.assertThat(p).isNotNull();
                return Selections.isPitcher(p);
            }};

    private static final Predicate<Player> IS_ON_40_MAN =
        new Predicate<Player>() {
            @Override
            public boolean apply(Player p) {
                Assertions.assertThat(p).isNotNull();
                return p.getOn40Man().or(Boolean.TRUE);
            }};

}
