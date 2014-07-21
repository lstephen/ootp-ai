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

    private LeagueBattingReport(BattingStats stats) {
        this.stats = stats;
    }

    @Override
    public void print(PrintWriter w) {
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


        Double a = (double) stats.getHits() + stats.getWalks() - stats.getHomeRuns();

        Double b = 1.4 * stats.getTotalBases() - .6 * stats.getHits() - 3 * stats.getHomeRuns() + .1 * stats.getWalks();

        Double c = (double) stats.getAtBats() - stats.getHits();

        Double d = (double) stats.getHomeRuns();

        Double z = (stats.getRuns() - d) / a;
        Double bsrFactor = (z * c/(1 - z)) / b;
        //Double bsrFactor = (((stats.getRuns() - d)*c)/(a - stats.getRuns() + d) / b);
        b = bsrFactor * b;

        Double bsr = a * b/(b + c) + d;

        w.format("bsf: %.2f%n", bsrFactor);
        w.format("bsr: %.2f%n", bsr);
        w.format("act: %d%n", stats.getRuns());
        w.println();


        FipBaseRuns.setFactor(bsrFactor);
        FipBaseRuns.setLeagueContext(stats);
        EraBaseRuns.setFactor(bsrFactor);
        EraBaseRuns.setLeagueContext(stats);
    }

    public static LeagueBattingReport create(Site site) {
        return new LeagueBattingReport(site.getLeagueBatting());
    }

}
