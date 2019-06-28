package com.github.lstephen.ootp.ai.stats;

import com.google.common.base.Preconditions;
import java.util.stream.IntStream;
import java.util.Arrays;

public class BaseRunsCoefficients {

  // http://gosu02.tripod.com/id108.html
  private static final Coefficients GOSU = new StaticCoefficients(0.78, 2.34, 3.9, 2.34, 0.039);

  private static final Coefficients SIMPLE = new StaticCoefficients(0.8, 2.1, 3.4, 1.8, 0.1);

  private static final Coefficients TANGO = new StaticCoefficients(0.726, 1.948, 3.134, 1.694, 0.052);

  private static Coefficients current = TANGO;

  public static double apply(double... stats) {
    return current.apply(stats);
  }

  public static double apply(BattingStats stats) {
    return current.apply(stats);
  }

  private static interface Coefficients {
    double apply(double... stats);

    default double apply(BattingStats stats) {
      return apply(stats.getSingles(), stats.getDoubles(), stats.getTriples(), stats.getHomeRuns(), stats.getWalks());
    }
  }

  private static class StaticCoefficients implements Coefficients {
    private final double[] coefficients;

    public StaticCoefficients(double... coefficients) {
      this.coefficients = coefficients;
    }

    public double apply(double... stats) {
      Preconditions.checkState(coefficients.length == 5, "Coefficients has length of " + coefficients.length);
      Preconditions.checkState(stats.length == 5, "Stats has length of " + stats.length);

      return IntStream.range(0, 6)
        .mapToDouble(i -> coefficients[i] * stats[i])
        .sum();
    }

    public String toString() {
      return String.format("StaticCoefficients(%s)", Arrays.toString(coefficients));
    }

  }

}
