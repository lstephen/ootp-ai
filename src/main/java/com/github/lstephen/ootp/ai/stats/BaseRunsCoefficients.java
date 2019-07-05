package com.github.lstephen.ootp.ai.stats;

import com.github.lstephen.ai.search.HillClimbing;
import com.github.lstephen.ai.search.action.Action;
import com.github.lstephen.ai.search.action.SequencedAction;
import com.github.lstephen.ootp.ai.io.Printable;
import com.github.lstephen.ootp.ai.ootp5.site.LeagueBatting;
import com.github.lstephen.ootp.ai.site.Site;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Ordering;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class BaseRunsCoefficients {

  // http://gosu02.tripod.com/id108.html
  private static final Coefficients GOSU = new Coefficients(0.78, 2.34, 3.9, 2.34, 0.039);

  private static final Coefficients SIMPLE = new Coefficients(0.8, 2.1, 3.4, 1.8, 0.1);

  private static final Coefficients TANGO = new Coefficients(0.726, 1.948, 3.134, 1.694, 0.052);

  private static Coefficients current = GOSU;

  public static double apply(double... stats) {
    return current.apply(stats);
  }

  public static double apply(BattingStats stats) {
    return current.apply(stats);
  }

  public static void calculate(Site site) {
    // B = (R - D)*C/(A - R + D)
    // run regression given that formula for B
    current = calculateHillClimb(site);

    BattingStats stats = site.getLeagueBatting();

    double a = (double) stats.getHits() + stats.getWalks() - stats.getHomeRuns();

    double b = BaseRunsCoefficients.apply(stats);

    double c = (double) stats.getOuts();
    double d = (double) stats.getHomeRuns();

    double bPrime = (stats.getRuns() - d) * c / (a - stats.getRuns() + d);

    current = current.withFactor(bPrime / b);
  }

  public static Coefficients calculateHillClimb(Site site) {
    LeagueBatting lb = new LeagueBatting(site, site.getDefinition().getLeague());

    List<BattingStats> history =
        IntStream.range(0, 5)
            .mapToObj(lb::extractYearsAgo)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .filter(h -> h.getRuns() > 0)
            .collect(Collectors.toList());

    Function<BattingStats, Double> actualB =
        bs -> {
          double a = (double) bs.getHits() + bs.getWalks() - bs.getHomeRuns();
          double c = (double) bs.getOuts();
          double d = (double) bs.getHomeRuns();
          double r = (double) bs.getRuns();
          return (r - d) * c / (a - r + d);
        };

    Function<double[], Double> rsme =
        ds ->
            Math.sqrt(
                history
                        .stream()
                        .map(
                            h ->
                                actualB.apply(h)
                                    - new Coefficients(
                                            ds[0] * 0.726,
                                            ds[0] * 1.948,
                                            ds[0] * 3.134,
                                            ds[1],
                                            ds[2])
                                        .apply(h))
                        .map(e -> e * e)
                        .mapToDouble(Double::doubleValue)
                        .sum()
                    / history.size());

    HillClimbing<double[]> hc =
        HillClimbing.<double[]>builder()
            .validator(
                ds -> ds.length == 3 && Arrays.stream(ds).allMatch(d -> d > -10.0 && d < 10.0))
            .heuristic(Ordering.natural().onResultOf(rsme).reverse())
            .actionGenerator(
                ds -> {
                  Collection<Action<double[]>> addTenths = addToAllIndexes(0.01, ds.length);
                  Collection<Action<double[]>> subTenths = addToAllIndexes(-0.01, ds.length);

                  Stream<Action<double[]>> tenths =
                      Stream.concat(
                          Stream.concat(subTenths.stream(), addTenths.stream()),
                          Stream.concat(
                              SequencedAction.allPairs(addTenths),
                              SequencedAction.allPairs(subTenths)));

                  return Stream.concat(
                      tenths,
                      Stream.concat(
                          addToAllIndexes(0.001, ds.length).stream(),
                          addToAllIndexes(-0.001, ds.length).stream()));
                })
            .build();

    double[] result = hc.search(new double[] {1.0, 1.694, 0.052});

    return new Coefficients(
        result[0] * 0.726, result[0] * 1.947, result[0] * 3.134, result[1], result[2]);
  }

  private static Collection<Action<double[]>> addToAllIndexes(double toAdd, int length) {
    return IntStream.range(0, length)
        .mapToObj(i -> addToIndex(toAdd, i))
        .collect(Collectors.toList());
  }

  private static Action<double[]> addToIndex(double toAdd, int idx) {
    return ds -> {
      double[] copy = Arrays.copyOf(ds, ds.length);
      copy[idx] += toAdd;
      return copy;
    };
  }

  public static Printable report() {
    return w -> {
      w.format("  GOSU: %s%n", GOSU);
      w.format("SIMPLE: %s%n", SIMPLE);
      w.format(" TANGO: %s%n", TANGO);
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
