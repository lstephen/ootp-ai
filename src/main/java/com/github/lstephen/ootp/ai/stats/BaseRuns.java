package com.github.lstephen.ootp.ai.stats;

/** @author lstephen */
public interface BaseRuns {

  // http://gosu02.tripod.com/id108.html
  double COEFFICIENT_SINGLE = 0.78;
  double COEFFICIENT_DOUBLE = 2.34;
  double COEFFICIENT_TRIPLE = 3.9;
  double COEFFICIENT_HOME_RUN = 2.34;
  double COEFFICIENT_WALK = 0.39;

  Double calculate(PitchingStats stats);
}
