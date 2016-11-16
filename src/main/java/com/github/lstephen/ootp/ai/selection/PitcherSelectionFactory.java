package com.github.lstephen.ootp.ai.selection;

import com.github.lstephen.ootp.ai.regression.Predictor;
import com.github.lstephen.ootp.ai.selection.rotation.RotationSelection;

/** @author lstephen */
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
