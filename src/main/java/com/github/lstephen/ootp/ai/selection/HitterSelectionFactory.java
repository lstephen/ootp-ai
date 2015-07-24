package com.github.lstephen.ootp.ai.selection;

import com.github.lstephen.ootp.ai.player.Player;
import com.github.lstephen.ootp.ai.player.Slot;
import com.github.lstephen.ootp.ai.regression.Predictions;
import com.github.lstephen.ootp.ai.stats.BattingStats;
import com.github.lstephen.ootp.ai.stats.TeamStats;

import com.google.common.base.Function;
import com.google.common.collect.Ordering;

public final class HitterSelectionFactory implements SelectionFactory {

    private final TeamStats<BattingStats> predictions;

    private final Function<Player, Integer> value;

    private HitterSelectionFactory(TeamStats<BattingStats> predictions, Function<Player, Integer> value) {
        this.predictions = predictions;
        this.value = value;
    }

    @Override
    public Selection create(Mode mode) {
        return new BestStartersSelection(
            mode.getHittingSlots(),
            predictions,
            value);
    }

    public Selection slot(Mode mode) {
        return SlotSelection
            .builder()
            .ordering(byOverall())
            .slots(mode.getHittingSlots())
            .size(mode.getHittingSlots().size())
            .fillToSize(Slot.H)
            .build();
    }

    public Ordering<Player> byOverall() {
        return Ordering
            .natural()
            .reverse()
            .onResultOf(value)
            .compound(Player.byTieBreak());
    }

    public static HitterSelectionFactory using(Predictions predictions) {
        return using(predictions.getAllBatting());
    }

    public static HitterSelectionFactory using(
        TeamStats<BattingStats> stats) {

        return using(stats, p -> stats.getOverall(p).getWobaPlus());
    }

    public static HitterSelectionFactory using(
        TeamStats<BattingStats> predictions,
        final Function<Player, Integer> value) {
        return new HitterSelectionFactory(predictions, value);
    }

}
