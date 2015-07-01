package com.ljs.ootp.ai.stats;

/**
 *
 * @author lstephen
 */
public interface BaseRuns {

  Double COEFFICIENT_SINGLE = 0.726;
  Double COEFFICIENT_DOUBLE = 1.948;
  Double COEFFICIENT_TRIPLE = 3.134;
  Double COEFFICIENT_HOME_RUN = 1.694;
  Double COEFFICIENT_WALK = 0.052;
  Double COEFFICIENT_STRIKEOUT = -0.057;
  Double COEFFICIENT_OUT = -0.004;

  Double calculate(PitchingStats stats);

}
