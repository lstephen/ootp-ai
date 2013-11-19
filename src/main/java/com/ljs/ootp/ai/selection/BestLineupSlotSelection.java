package com.ljs.ootp.ai.selection;

import com.google.common.collect.ImmutableMultimap;
import com.ljs.ootp.ai.player.Player;
import com.ljs.ootp.ai.player.Slot;
import com.ljs.ootp.ai.regression.Predictions;

/**
 *
 * @author lstephen
 */
public class BestLineupSlotSelection implements Selection {

    private Predictions predictions;

    private BestLineupSlotSelection(Predictions predictions) {
        this.predictions = predictions;
    }

    @Override
    public ImmutableMultimap<Slot, Player> select(Iterable<Player> forced, Iterable<Player> available) {

        // Determine best lineups
        // Sort all players by value
        // Assign to slots as possible

        // Select for the remaining slots as usual
        // Might it be possible to iterate here?
        // e.g., select the best lineups using players that can fill the reaming slots
        // The assign as possible, select for the remaining again

        // Fallback to selecting for the remaining as usual if we run out
        // of players to fill the remaining slots

        return null;
    }





}
