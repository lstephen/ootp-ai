package com.ljs.ootp.ai.report;

import com.google.common.collect.ImmutableMap;
import com.ljs.ootp.ai.io.Printable;
import com.ljs.ootp.ai.site.Site;
import com.ljs.ootp.ai.stats.BattingStats;
import com.ljs.ootp.ai.stats.EraBaseRuns;
import com.ljs.ootp.ai.stats.FipBaseRuns;
import com.ljs.ootp.ai.stats.Woba;
import java.io.PrintWriter;

/**
 *
 * @author lstephen
 */
public class LeagueBattingReport implements Printable {

    private final BattingStats stats;

    private final Double bsrFactor;

    private LeagueBattingReport(BattingStats stats) {
        this.stats = stats;

        Double a = (double) stats.getHits() + stats.getWalks() - stats.getHomeRuns();

        Double b =
            .8 *stats.getSingles()
            + 2.1 * stats.getDoubles()
            + 3.4 * stats.getTriples()
            + 1.8 * stats.getHomeRuns()
            + .1 * stats.getWalks();

        Double c = (double) stats.getOuts();

        Double d = (double) stats.getHomeRuns();

        Double z = (stats.getRuns() - d) / a;
        bsrFactor = (z * c/(1 - z)) / b;
    }

    @Override
    public void print(PrintWriter w) {

        w.format("bsf: %.2f%n", bsrFactor);
        w.format("act: %d%n", stats.getRuns());
        w.println();

        // TODO: Could these be calculated from baseruns instead?
        Double rperOut = (double) stats.getRuns() / stats.getOuts();
        Double runBB = rperOut + 0.14;
        Double run1B = runBB + 0.155;
        Double run2B = run1B + 0.3;
        Double run3B = run2B + 0.27;
        Double runHR = 1.4;

        w.println();
        w.format("RperO: %.2f%n", rperOut);
        w.format("runBB: %.2f%n", runBB);
        w.format("run1B: %.2f%n", run1B);
        w.format("run2B: %.2f%n", run2B);
        w.format("run3B: %.2f%n", run3B);
        w.format("runHR: %.2f%n", runHR);

        runBB = getRunValueBB();
        run1B = getRunValue1B();
        run2B = getRunValue2B();
        run3B = getRunValue3B();
        runHR = getRunValueHR();

        w.println();
        w.format("RperO: %.2f%n", rperOut);
        w.format("runBB: %.2f%n", runBB);
        w.format("run1B: %.2f%n", run1B);
        w.format("run2B: %.2f%n", run2B);
        w.format("run3B: %.2f%n", run3B);
        w.format("runHR: %.2f%n", runHR);

        Double runsMinus = (runBB * stats.getWalks()
            + run1B * stats.getSingles()
            + run2B * stats.getDoubles()
            + run3B * stats.getTriples()
            + runHR * stats.getHomeRuns())
            / stats.getOuts();

        Double runsPlus = (runBB * stats.getWalks()
            + run1B * stats.getSingles()
            + run2B * stats.getDoubles()
            + run3B * stats.getTriples()
            + runHR * stats.getHomeRuns())
            / (stats.getWalks() + stats.getHits());

        Double wobaScale = 1 / (runsPlus + runsMinus);

        Double bipValue = stats.getBabip() * runsPlus - (1.0 - stats.getBabip()) * runsMinus;

        w.format("rnBIP: %.2f%n", bipValue);

        w.println();
        w.format("runs-: %.2f%n", runsMinus);
        w.format("runs+: %.2f%n", runsPlus);
        w.format("wobas: %.2f%n", wobaScale);


        Double wobaBB = (runBB + runsMinus) * wobaScale;
        Double woba1B = (run1B + runsMinus) * wobaScale;
        Double woba2B = (run2B + runsMinus) * wobaScale;
        Double woba3B = (run3B + runsMinus) * wobaScale;
        Double wobaHR = (runHR + runsMinus) * wobaScale;

        w.println();
        w.format("wobaBB: %.2f%n", wobaBB);
        w.format("woba1B: %.2f%n", woba1B);
        w.format("woba2B: %.2f%n", woba2B);
        w.format("woba3B: %.2f%n", woba3B);
        w.format("wobaHR: %.2f%n", wobaHR);

        Woba.setConstants(ImmutableMap
            .<Woba.Event, Double>builder()
            .put(Woba.Event.WALK, wobaBB)
            .put(Woba.Event.SINGLE, woba1B)
            .put(Woba.Event.DOUBLE, woba2B)
            .put(Woba.Event.TRIPLE, woba3B)
            .put(Woba.Event.HOME_RUN, wobaHR)
            .build());

        w.println();

        FipBaseRuns.setFactor(bsrFactor);
        FipBaseRuns.setLeagueContext(stats);
        EraBaseRuns.setFactor(bsrFactor);
        EraBaseRuns.setLeagueContext(stats);
    }

    private Double getRunValueBB() {
        BattingStats plusOne = new BattingStats();
        plusOne.setWalks(1);

        return baseRuns(stats.add(plusOne)) - stats.getRuns();
    }

    private Double getRunValue1B() {
        BattingStats plusOne = new BattingStats();
        plusOne.setHits(1);
        plusOne.setAtBats(1);

        return baseRuns(stats.add(plusOne)) - stats.getRuns();
    }

    private Double getRunValue2B() {
        BattingStats plusOne = new BattingStats();
        plusOne.setHits(1);
        plusOne.setAtBats(1);
        plusOne.setDoubles(1);

        return baseRuns(stats.add(plusOne)) - stats.getRuns();
    }

    private Double getRunValue3B() {
        BattingStats plusOne = new BattingStats();
        plusOne.setHits(1);
        plusOne.setAtBats(1);
        plusOne.setTriples(1);

        return baseRuns(stats.add(plusOne)) - stats.getRuns();
    }

    private Double getRunValueHR() {
        BattingStats plusOne = new BattingStats();
        plusOne.setHits(1);
        plusOne.setAtBats(1);
        plusOne.setHomeRuns(1);

        return baseRuns(stats.add(plusOne)) - stats.getRuns();
    }


    private Double baseRuns(BattingStats stats) {

        Double a = (double) stats.getHits() + stats.getWalks() - stats.getHomeRuns();

        Double b =
            .8 * stats.getSingles()
            + 2.1 * stats.getDoubles()
            + 3.4 * stats.getTriples()
            + 1.8*stats.getHomeRuns()
            + .1 * stats.getWalks();

        Double c = (double) stats.getOuts();

        Double d = (double) stats.getHomeRuns();

        b = bsrFactor * b;

        return a * b/(b+c) + d;
    }

    public static LeagueBattingReport create(Site site) {
        return new LeagueBattingReport(site.getLeagueBatting());
    }

}
