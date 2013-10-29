package com.ljs.scratch.ootp.stats;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 *
 * @author lstephen
 */
public class PitchingStats implements Stats<PitchingStats> {

    // TODO: Base this on the league itself
    private static final double FIP_FUDGE_FACTOR = 3.10;

    private int atBats;

    private int hits;

    private int doubles;

    private int triples;

    private int strikeouts;

    private int walks;

    private int homeRuns;

    @JsonIgnore
    private PitchingStats leaguePitching;

    public void setAtBats(int atBats) { this.atBats = atBats; }

    public Integer getHits() { return hits; }
    public void setHits(int hits) { this.hits = hits; }

    public void setDoubles(int doubles) { this.doubles = doubles; }
    public void setTriples(int triples) { this.triples = triples; }
    public void setStrikeouts(int strikeouts) { this.strikeouts = strikeouts; }
    public void setWalks(int walks) { this.walks = walks; }
    public void setHomeRuns(int homeRuns) { this.homeRuns = homeRuns; }

    public void setLeaguePitching(PitchingStats leaguePitching) {
        this.leaguePitching = leaguePitching;
    }

    public int getPlateAppearances() { return atBats + walks; }

    public int getInningsPitched() {
        return (atBats - hits) / 3;
    }

    public double getHitsPerPlateAppearance() {
        return perPlateAppearance(hits);
    }

    public double getDoublesPerPlateAppearance() {
        return perPlateAppearance(doubles);
    }

    public double getDoublesPerHit() {
        return perHit(doubles);
    }

    public double getStrikeoutsPerPlateAppearance() {
        return perPlateAppearance(strikeouts);
    }

    public double getWalksPerPlateAppearance() {
        return perPlateAppearance(walks);
    }

    public double getHomeRunsPerPlateAppearance() {
        return perPlateAppearance(homeRuns);
    }

    public double getHomeRunsPerHit() {
        return perHit(homeRuns);
    }

    public double getBabip() {
        if (atBats - strikeouts - homeRuns == 0) {
            return 0;
        } else {
            return (double) (hits - homeRuns)
                / (atBats - strikeouts - homeRuns);
        }
    }

    public double getFip() {
        return (double) (13 * homeRuns + 3 * walks - 2 * strikeouts)
            / getInningsPitched() + FIP_FUDGE_FACTOR;
    }

    public int getFipPlus() {
        return (int) (leaguePitching.getFip() * 100 / getFip());
    }

    public double getWobaAgainst() {
        return perPlateAppearance(
              0.7 * walks
            + 0.9 * (hits - doubles - triples - homeRuns)
            + 1.3 * (doubles + triples)
            + 2.0 * homeRuns);
    }

    public int getWobaPlusAgainst() {
        return (int) (leaguePitching.getWobaAgainst() * 100 / getWobaAgainst());
    }

    private double perPlateAppearance(double value) {
        return getPlateAppearances() == 0
            ? 0
            : value / getPlateAppearances();
    }

    private double perHit(double value) {
        return getHits() == 0 ? 0 : value / getHits();
    }

    @Override
    public PitchingStats multiply(double factor) {
        PitchingStats stats = new PitchingStats();
        stats.leaguePitching = leaguePitching;
        stats.atBats = (int) (factor * atBats);
        stats.hits = (int) (factor * hits);
        stats.doubles = (int) (factor * doubles);
        stats.strikeouts = (int) (factor * strikeouts);
        stats.walks = (int) (factor * walks);
        stats.homeRuns = (int) (factor * homeRuns);
        return stats;
    }

    @Override
    public PitchingStats add(PitchingStats rhs) {
        PitchingStats stats = new PitchingStats();
        stats.leaguePitching = leaguePitching;
        stats.atBats = rhs.atBats + atBats;
        stats.hits = rhs.hits + hits;
        stats.doubles = rhs.doubles + doubles;
        stats.strikeouts = rhs.strikeouts + strikeouts;
        stats.walks = rhs.walks + walks;
        stats.homeRuns = rhs.homeRuns + homeRuns;
        return stats;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}
