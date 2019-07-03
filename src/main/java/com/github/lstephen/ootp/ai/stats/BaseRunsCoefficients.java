package com.github.lstephen.ootp.ai.stats;

import com.github.lstephen.ootp.ai.io.Printable;
import com.github.lstephen.ootp.ai.ootp5.site.LeagueBatting;
import com.github.lstephen.ootp.ai.site.Site;
import com.google.common.base.Preconditions;
import com.google.common.primitives.Doubles;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;
import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression;

public class BaseRunsCoefficients {

  // http://gosu02.tripod.com/id108.html
  private static final Coefficients GOSU = new Coefficients(0.78, 2.34, 3.9, 2.34, 0.039);

  private static final Coefficients SIMPLE = new Coefficients(0.8, 2.1, 3.4, 1.8, 0.1);

  private static final Coefficients TANGO = new Coefficients(0.726, 1.948, 3.134, 1.694, 0.052);

  private static Coefficients regressed;

  private static Coefficients current = TANGO;

  public static double apply(double... stats) {
    return current.apply(stats);
  }

  public static double apply(BattingStats stats) {
    return current.apply(stats);
  }

  public static void calculate(Site site) {
    // B = (R - D)*C/(A - R + D)
    // run regression given that formula for B
    LeagueBatting lb = new LeagueBatting(site, site.getDefinition().getLeague());
    List<double[]> inputs = new ArrayList<>();
    List<Double> expectedBs = new ArrayList<>();
    for (int i = 0; i < 5; i++) {
      final int yearsAgo = i;
      lb.extractYearsAgo(i)
          .ifPresent(
              bs -> {
                if (bs.getRuns() == 0) {
                  return;
                }


                double a = (double) bs.getHits() + bs.getWalks() - bs.getHomeRuns();
                double c = (double) bs.getOuts();
                double d = (double) bs.getHomeRuns();
                double r = (double) bs.getRuns();
                double expectedB = (r - d) * c / (a - r + d);

                for (int j = 0; j < 5-yearsAgo; j++) {
                  inputs.add(
                      new double[] {
                        bs.getSingles(),
                        bs.getDoubles(),
                        bs.getTriples(),
                        bs.getHomeRuns(),
                        bs.getWalks()
                      });
                  expectedBs.add(expectedB);
                }
              });
    }

    OLSMultipleLinearRegression regress = new OLSMultipleLinearRegression();
    regress.setNoIntercept(true);
    regress.newSampleData(Doubles.toArray(expectedBs), inputs.toArray(new double[][] {}));
    regressed = new Coefficients(regress.estimateRegressionParameters());

    BattingStats stats = site.getLeagueBatting();

    double a = (double) stats.getHits() + stats.getWalks() - stats.getHomeRuns();

    double b = BaseRunsCoefficients.apply(stats);

    double c = (double) stats.getOuts();
    double d = (double) stats.getHomeRuns();

    double bPrime = (stats.getRuns() - d) * c / (a - stats.getRuns() + d);

    current = current.withFactor(bPrime / b);
  }

  public static Printable report() {
    return w -> {
      w.format("  GOSU: %s%n", GOSU);
      w.format("SIMPLE: %s%n", SIMPLE);
      w.format(" TANGO: %s%n", TANGO);
      w.format("REGRES: %s%n", regressed);
      w.format("CURENT: %s%n", current);
    };
  }

  private static class Coefficients {
    private final double factor;
    private final double[] coefficients;

    public Coefficients(double... coefficients) {
      this(1.0, coefficients);
    }

    private Coefficients(double factor, double[] coefficients) {
      this.factor = factor;
      this.coefficients = coefficients;
    }

    public double apply(BattingStats stats) {
      return apply(
          stats.getSingles(),
          stats.getDoubles(),
          stats.getTriples(),
          stats.getHomeRuns(),
          stats.getWalks());
    }

    public double apply(double... stats) {
      Preconditions.checkState(
          coefficients.length == 5, "Coefficients has length of " + coefficients.length);
      Preconditions.checkState(stats.length == 5, "Stats has length of " + stats.length);

      return factor * IntStream.range(0, 5).mapToDouble(i -> coefficients[i] * stats[i]).sum();
    }

    public Coefficients withFactor(double factor) {
      return new Coefficients(factor, coefficients);
    }

    public String toString() {
      return String.format("Coefficients(%.2f, %s)", factor, Arrays.toString(coefficients));
    }
  }
}
