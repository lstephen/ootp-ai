package com.ljs.ootp.ai.stats;

/**
 *
 * @author lstephen
 */
public class FipBaseRuns implements BaseRuns {

    private static final FipBaseRuns INSTANCE = new FipBaseRuns();

    private Double factor;

    private BattingStats context;

    private FipBaseRuns() { }

    public Double calculate(PitchingStats stats) {
        Double hits = context.getBabip() * (stats.getPlateAppearances() - stats.getStrikeouts() - stats.getWalks() - stats.getHomeRuns());
        Double doubles = context.getDoublesPerHit() * hits;
        Double triples = context.getTriplesPerHit() * hits;
        Double singles = hits - doubles - triples - stats.getHomeRuns();

        Double a = hits + stats.getWalks() - stats.getHomeRuns();
        Double b = factor * (.8*singles + 2.1*doubles + 3.4*triples + 1.8*stats.getHomeRuns() + .1*stats.getWalks());
        Double c = (double) stats.getOuts();
        Double d = (double) stats.getHomeRuns();

        Double bsr = a * b/(b+c) + d;

        return bsr / stats.getInningsPitched() * 9;
    }

    public static FipBaseRuns get() {
        return INSTANCE;
    }

    public static void setFactor(Double factor) {
        get().factor = factor;
    }

    public static void setLeagueContext(BattingStats stats) {
        get().context = stats;
    }

}
