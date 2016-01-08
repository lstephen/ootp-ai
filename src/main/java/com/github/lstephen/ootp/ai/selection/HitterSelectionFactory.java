package com.github.lstephen.ootp.ai.selection;

import com.github.lstephen.ootp.ai.player.Player;
import com.github.lstephen.ootp.ai.player.Slot;
import com.github.lstephen.ootp.ai.regression.Predictions;
import com.github.lstephen.ootp.ai.stats.BattingStats;
import com.github.lstephen.ootp.ai.stats.TeamStats;

import com.google.common.base.Function;
import com.google.common.collect.Ordering;

public final class HitterSelectionFactory implements SelectionFactory {

    private final Predictions predictions;

    private final Function<Player, Integer> value;

    private HitterSelectionFactory(Predictions predictions, Function<Player, Integer> value) {
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
        return using(predictions, p -> predictions.getOverallHitting(p));
    }

    public static HitterSelectionFactory using(
        Predictions predictions,
        final Function<Player, Integer> value) {
        return new HitterSelectionFactory(predictions, value);
    }

}
