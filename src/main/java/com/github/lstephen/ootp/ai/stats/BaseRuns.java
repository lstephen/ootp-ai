package com.github.lstephen.ootp.ai.stats;

/** @author lstephen */
public interface BaseRuns {

  double COEFFICIENT_SINGLE = 0.8;
  double COEFFICIENT_DOUBLE = 2.1;
  double COEFFICIENT_TRIPLE = 3.4;
  double COEFFICIENT_HOME_RUN = 1.8;
  double COEFFICIENT_WALK = 0.1;

  Double calculate(PitchingStats stats);
}
