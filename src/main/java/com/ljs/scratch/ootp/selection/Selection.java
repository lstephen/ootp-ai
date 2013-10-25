package com.ljs.scratch.ootp.selection;

import com.google.common.collect.ImmutableMultimap;
import com.ljs.scratch.ootp.player.Player;

public interface Selection {

    ImmutableMultimap<Slot, Player> select(
        Iterable<Player> forced, Iterable<Player> available);

}
