package com.github.lstephen.ootp.ai.stats;

/** @author lstephen */
public class EraBaseRuns implements BaseRuns {
  private static final EraBaseRuns INSTANCE = new EraBaseRuns();

  private BattingStats context;

  private EraBaseRuns() {}

  public Double calculate(PitchingStats stats) {
    Double hits = (double) stats.getHits();
    Double doubles = (double) stats.getDoubles();
    Double triples = context.getTriplesPerHit() * hits;

    Double a = hits + stats.getWalks() - stats.getHomeRuns();

    Double b =
        BaseRunsCoefficients.apply(
            stats.getSingles(), doubles, triples, stats.getHomeRuns(), stats.getWalks());

    Double c = (double) stats.getOuts();
    Double d = (double) stats.getHomeRuns();

    Double bsr = a * b / (b + c) + d;

    return bsr / stats.getOuts() * 27;
  }

  public static EraBaseRuns get() {
    return INSTANCE;
  }

  public static void setLeagueContext(BattingStats stats) {
    get().context = stats;
  }
}
