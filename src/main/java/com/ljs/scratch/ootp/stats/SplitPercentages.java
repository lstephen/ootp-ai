package com.ljs.scratch.ootp.stats;

import com.google.common.base.Charsets;
import com.ljs.scratch.ootp.html.Site;
import com.ljs.scratch.ootp.regression.History;
import java.io.IOException;
import java.io.OutputStream;

/**
 *
 * @author lstephen
 */
public final class SplitPercentages {

    private final Site site;

    private int vsLhp;
    private int vsRhp;

    private int vsLhb;
    private int vsRhb;

    private SplitPercentages(Site site) {
        this.site = site;
    }

    public <S extends Stats<S>> S combine(S vsLeft, S vsRight) {
        if (BattingStats.class.isInstance(vsLeft)) {
            return combine(vsLeft, getVsLhpPercentage(), vsRight, getVsRhbPercentage());
        } else if (PitchingStats.class.isInstance(vsLeft)) {
            return combine(vsLeft, getVsLhbPercentage(), vsRight, getVsRhbPercentage());
        } else {
            throw new IllegalStateException();
        }
    }

    private <S extends Stats<S>> S combine(S vsLeft, double lpct, S vsRight, double rpct) {
        return vsLeft
            .multiply(lpct * 10)
            .add(vsRight.multiply(rpct * 10))
            .multiply(0.1);
    }

    public double getVsLhbPercentage() {
        return (double) vsLhb / (vsLhb + vsRhb);
    }

    public double getVsRhbPercentage() {
        return (double) vsRhb / (vsLhb + vsRhb);
    }

    public double getVsLhpPercentage() {
        return (double) vsLhp / (vsLhp + vsRhp);
    }

    public double getVsRhpPercentage() {
        return (double) vsRhp / (vsLhp + vsRhp);
    }

    private void load() {
        loadBatting(site.getTeamBatting().extract());
        loadPitching(site.getTeamPitching().extract());

        History history = new History();

        int currentSeason = site.getDate().getYear();

        for (TeamStats<BattingStats> h : history.loadBatting(site, currentSeason, 5)) {
            loadBatting(h);
        }

        for (TeamStats<PitchingStats> p : history.loadPitching(site, currentSeason, 5)) {
            loadPitching(p);
        }
    }

    private void loadBatting(TeamStats<BattingStats> battingStats) {
        for (SplitStats<BattingStats> stats : battingStats.getSplits()) {
            vsLhp += stats.getVsLeft().getPlateAppearances();
            vsRhp += stats.getVsRight().getPlateAppearances();
        }
    }

    private void loadPitching(TeamStats<PitchingStats> pitchingStats) {
        for (SplitStats<PitchingStats> stats : pitchingStats.getSplits()) {
            vsLhb += stats.getVsLeft().getPlateAppearances();
            vsRhb += stats.getVsRight().getPlateAppearances();
        }
    }

    public void print(OutputStream out) throws IOException {
        out.write(String
            .format(
                "%nBatting: %d/%d %.3f/%.3f%n",
                vsLhp,
                vsRhp,
                getVsLhpPercentage(),
                getVsRhpPercentage())
            .getBytes(Charsets.UTF_8));

        out.write(String
            .format(
                "Pitching: %d/%d %.3f/%.3f%n",
                vsLhb,
                vsRhb,
                getVsLhbPercentage(),
                getVsRhbPercentage())
            .getBytes(Charsets.UTF_8));
    }

    public static SplitPercentages create(Site site) {
        SplitPercentages pcts = new SplitPercentages(site);
        pcts.load();
        return pcts;
    }

}
