package com.github.lstephen.ootp.ai.selection;

import com.google.common.base.Function;
import com.google.common.base.Throwables;
import com.google.common.collect.Iterables;
import com.google.common.collect.Ordering;
import com.github.lstephen.ootp.ai.player.Player;
import com.github.lstephen.ootp.ai.player.Slot;
import com.github.lstephen.ootp.ai.regression.Predictor;
import com.github.lstephen.ootp.ai.selection.rotation.RotationSelection;
import com.github.lstephen.ootp.ai.stats.PitchingStats;
import com.github.lstephen.ootp.ai.stats.TeamStats;
import java.util.Arrays;

/**
 *
 * @author lstephen
 */
public final class PitcherSelectionFactory implements SelectionFactory {

    private final Predictor predictor;

    public PitcherSelectionFactory(Predictor predictor) {
        this.predictor = predictor;
    }

    @Override
    public Selection create(Mode mode) {
        return RotationSelection.forMode(mode, predictor);
    }

}
