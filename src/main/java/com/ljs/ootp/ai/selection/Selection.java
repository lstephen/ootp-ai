package com.ljs.ootp.ai.selection;

import com.ljs.ootp.ai.player.Slot;
import com.google.common.collect.ImmutableMultimap;
import com.ljs.ootp.ai.player.Player;

public interface Selection {

    ImmutableMultimap<Slot, Player> select(
        Iterable<Player> forced, Iterable<Player> available);

}
