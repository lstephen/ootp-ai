package com.github.lstephen.ootp.ai.selection;

import com.github.lstephen.ootp.ai.player.Slot;
import com.google.common.collect.ImmutableMultimap;
import com.github.lstephen.ootp.ai.player.Player;

public interface Selection {

    ImmutableMultimap<Slot, Player> select(
        Iterable<Player> forced, Iterable<Player> available);

}
