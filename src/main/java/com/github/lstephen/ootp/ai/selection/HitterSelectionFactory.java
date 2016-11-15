package com.github.lstephen.ootp.ai.selection;

import com.github.lstephen.ootp.ai.player.Player;
import com.github.lstephen.ootp.ai.player.Slot;
import com.github.lstephen.ootp.ai.regression.Predictor;
import com.github.lstephen.ootp.ai.stats.BattingStats;
import com.github.lstephen.ootp.ai.stats.TeamStats;

import com.google.common.base.Function;
import com.google.common.collect.Ordering;

public final class HitterSelectionFactory implements SelectionFactory {

    private final Predictor predictor;

    public HitterSelectionFactory(Predictor predictor) {
        this.predictor = predictor;
    }

    @Override
    public Selection create(Mode mode) {
        return new BestStartersSelection(
            mode.getHittingSlots().size(),
            predictor);
    }

}
