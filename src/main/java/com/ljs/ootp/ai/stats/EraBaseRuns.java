package com.ljs.ootp.ai.stats;

/**
 *
 * @author lstephen
 */
public class EraBaseRuns implements BaseRuns {
    private static final EraBaseRuns INSTANCE = new EraBaseRuns();

    private Double factor;

    private BattingStats context;

    private EraBaseRuns() { }

    public Double calculate(PitchingStats stats) {
        Double hits = (double) stats.getHits();
        Double doubles = (double) stats.getDoubles();
        Double triples = context.getTriplesPerHit() * hits;

        Double tb = hits + doubles + 2 * triples + 3 * stats.getHomeRuns();

        Double a = hits + stats.getWalks() - stats.getHomeRuns();
        Double b = factor * (1.4 * tb - .6 * hits - 3 * stats.getHomeRuns() + .1 * stats.getWalks());
        Double c = (double) stats.getOuts();
        Double d = (double) stats.getHomeRuns();

        Double bsr = a * b/(b+c) + d;

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
