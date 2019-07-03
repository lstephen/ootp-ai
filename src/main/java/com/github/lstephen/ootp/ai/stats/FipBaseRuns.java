package com.github.lstephen.ootp.ai.stats;

/** @author lstephen */
public class FipBaseRuns implements BaseRuns {

  private static final FipBaseRuns INSTANCE = new FipBaseRuns();

  private BattingStats context;

  private FipBaseRuns() {}

  public Double calculate(PitchingStats stats) {
    Double hits =
        context.getBabip()
            * (stats.getPlateAppearances()
                - stats.getStrikeouts()
                - stats.getWalks()
                - stats.getHomeRuns());
    Double doubles = context.getDoublesPerHit() * hits;
    Double triples = context.getTriplesPerHit() * hits;
    Double singles = hits - doubles - triples - stats.getHomeRuns();

    Double a = hits + stats.getWalks() - stats.getHomeRuns();

    Double b = BaseRunsCoefficients.apply(singles, doubles, triples, stats.getHomeRuns(), stats.getWalks());

    Double c = (double) stats.getOuts();

    Double d = (double) stats.getHomeRuns();

    Double bsr = a * b / (b + c) + d;

    return bsr / stats.getInningsPitched() * 9;
  }

  public static FipBaseRuns get() {
    return INSTANCE;
  }

  public static void setLeagueContext(BattingStats stats) {
    get().context = stats;
  }
}
