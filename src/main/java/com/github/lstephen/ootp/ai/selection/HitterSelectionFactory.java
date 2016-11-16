package com.github.lstephen.ootp.ai.selection;

import com.github.lstephen.ootp.ai.regression.Predictor;

public final class HitterSelectionFactory implements SelectionFactory {

  private final Predictor predictor;

  public HitterSelectionFactory(Predictor predictor) {
    this.predictor = predictor;
  }

  @Override
  public Selection create(Mode mode) {
    return new BestStartersSelection(mode.getHittingSlots().size(), predictor);
  }
}
