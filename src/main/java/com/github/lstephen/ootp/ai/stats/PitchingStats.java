package com.github.lstephen.ootp.ai.stats;

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
    public void setHits(long hits) { this.hits = (int) hits; }

    public int getSingles() { return hits - doubles - triples - homeRuns; }

    public int getDoubles() { return doubles; }
    public void setDoubles(long doubles) { this.doubles = (int) doubles; }

    public int getTriples() { return triples; }
    public void setTriples(long triples) { this.triples = (int) triples; }

    public int getStrikeouts() { return strikeouts; }
    public void setStrikeouts(long strikeouts) { this.strikeouts = (int) strikeouts; }

    public int getWalks() { return walks; }
    public void setWalks(long walks) { this.walks = (int) walks; }

    public int getHomeRuns() { return homeRuns; }
    public void setHomeRuns(long homeRuns) { this.homeRuns = (int) homeRuns; }

    public void setLeaguePitching(PitchingStats leaguePitching) {
        this.leaguePitching = leaguePitching;
    }

    public int getPlateAppearances() { return atBats + walks; }

    public int getOuts() { return atBats - hits; }

    public int getInningsPitched() {
        return (atBats - hits) / 3;
    }

    public double getHitsPerPlateAppearance() {
        return perPlateAppearance(hits);
    }

    public double getHitsPerNine() {
        return perInnings(hits) * 9;
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

    public double getStrikeoutsPerNine() {
        return perInnings(strikeouts) * 9;
    }

    public double getWalksPerPlateAppearance() {
        return perPlateAppearance(walks);
    }

    public double getWalksPerNine() {
        return perInnings(walks) * 9;
    }

    public double getHomeRunsPerNine() {
        return perInnings(homeRuns) * 9;
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

    public Double getBaseRuns(BaseRuns brs) {
        return brs.calculate(this);
    }

    public Integer getBaseRunsPlus(BaseRuns brs) {
        return (int) (leaguePitching.getBaseRuns(brs) * 100 / getBaseRuns(brs));
    }

    public double getFip() {
        return (double) (13 * homeRuns + 3 * walks - 2 * strikeouts)
            / getInningsPitched() + FIP_FUDGE_FACTOR;
    }

    public int getFipPlus() {
        return (int) (leaguePitching.getFip() * 100 / getFip());
    }

    public double getWobaAgainst() {
        return Woba.get().calculate(this);
        /*return perPlateAppearance(
              0.7 * walks
            + 0.9 * (hits - doubles - triples - homeRuns)
            + 1.3 * (doubles + triples)
            + 2.0 * homeRuns);*/
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

    private double perInnings(double value) {
        return getInningsPitched() == 0 ? 0 : value / getInningsPitched();
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
