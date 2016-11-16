package com.github.lstephen.ootp.ai.stats;

/** @author lstephen */
public class EraBaseRuns implements BaseRuns {
  private static final EraBaseRuns INSTANCE = new EraBaseRuns();

  private Double factor;

  private BattingStats context;

  private EraBaseRuns() {}

  public Double calculate(PitchingStats stats) {
    Double hits = (double) stats.getHits();
    Double doubles = (double) stats.getDoubles();
    Double triples = context.getTriplesPerHit() * hits;

    Double a = hits + stats.getWalks() - stats.getHomeRuns();

    Double b =
        factor
            * (COEFFICIENT_SINGLE * stats.getSingles()
                + COEFFICIENT_DOUBLE * doubles
                + COEFFICIENT_TRIPLE * triples
                + COEFFICIENT_HOME_RUN * stats.getHomeRuns()
                + COEFFICIENT_WALK * stats.getWalks()
                + COEFFICIENT_STRIKEOUT * stats.getStrikeouts()
                + COEFFICIENT_OUT * (stats.getOuts() - stats.getStrikeouts()));

    Double c = (double) stats.getOuts();
    Double d = (double) stats.getHomeRuns();

    Double bsr = a * b / (b + c) + d;

    return bsr / stats.getOuts() * 27;
  }

  public static EraBaseRuns get() {
    return INSTANCE;
  }

  public static void setFactor(Double factor) {
    get().factor = factor;
  }

  public static void setLeagueContext(BattingStats stats) {
    get().context = stats;
  }
}
