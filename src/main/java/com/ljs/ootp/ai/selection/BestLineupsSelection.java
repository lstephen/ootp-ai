package com.ljs.ootp.ai.selection;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multiset;
import com.ljs.ootp.ai.player.Player;
import com.ljs.ootp.ai.player.Slot;
import com.ljs.ootp.ai.selection.lineup.AllLineups;
import com.ljs.ootp.ai.selection.lineup.LineupSelection;
import com.ljs.ootp.ai.stats.BattingStats;
import com.ljs.ootp.ai.stats.SplitPercentages;
import com.ljs.ootp.ai.stats.TeamStats;

/**
 *
 * @author lstephen
 */
public class BestLineupsSelection implements Selection {

    private final Multiset<Slot> slots;

    private final Function<Player, Integer> value;

    private final TeamStats<BattingStats> predictions;

    private final SplitPercentages splits;

    public BestLineupsSelection(Multiset<Slot> slots, Function<Player, Integer> value, TeamStats<BattingStats> predictions, SplitPercentages splits) {
        this.slots = slots;
        this.value = value;
        this.predictions = predictions;
        this.splits = splits;
    }

    @Override
    public ImmutableMultimap<Slot, Player> select(
        Iterable<Player> forced,
        Iterable<Player> available) {

        AllLineups best = new LineupSelection(predictions).select(Selections.onlyHitters(available));

        return new PrioritzeStarterSelection(slots, value, predictions, splits).select(Iterables.concat(forced, best.getCommonPlayers()), available);
    }
}
