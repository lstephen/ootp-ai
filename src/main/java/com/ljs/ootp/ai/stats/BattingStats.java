package com.ljs.ootp.ai.stats;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.text.DecimalFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 *
 * @author lstephen
 */
public class BattingStats implements Stats<BattingStats> {

    private int atBats;

    private int hits;

    private int doubles;

    private int triples;

    private int homeRuns;

    private int walks;

    @JsonIgnore
    private BattingStats leagueBatting;

    public void setAtBats(int atBats) { this.atBats = atBats; }

    public int getHits() { return hits; }
    public void setHits(int hits) { this.hits = hits; }

    public int getDoubles() { return doubles; }
    public void setDoubles(int doubles) { this.doubles = doubles; }

    public int getTriples() { return triples; }
    public void setTriples(int triples) { this.triples = triples; }

    public void setHomeRuns(int homeRuns) { this.homeRuns = homeRuns; }

    public void setWalks(int walks) { this.walks = walks; }

    public void setLeagueBatting(BattingStats leagueBatting) {
        this.leagueBatting = leagueBatting;
    }

    public int getPlateAppearances() { return atBats + walks; }

    public double getHitsPerPlateAppearance() {
        return perPlateAppearance(hits);
    }

    public double getExtraBaseHitsPerPlateAppearance() {
        return perPlateAppearance(doubles + triples);
    }

    public double getDoublesPerHit() {
        return perHit(doubles);
    }

    public double getHomeRunsPerPlateAppearance() {
        return perPlateAppearance(homeRuns);
    }

    public double getHomeRunsPerHit() {
        return perHit(homeRuns);
    }

    public double getWalksPerPlateAppearance() {
        return perPlateAppearance(walks);
    }

    public double getAverage() {
        return atBats == 0 ? 0 : (double) hits / atBats;
    }

    public double getOnBasePercentage() {
        return perPlateAppearance(walks + hits);
    }

    public double getSluggingPercentage() {
        return atBats == 0
            ? 0
            : (double) (hits + doubles + 2 * triples + 3 * homeRuns) / atBats;
    }

    public double getWoba() {
        return perPlateAppearance(
              0.7 * walks
            + 0.9 * (hits - doubles - triples - homeRuns)
            + 1.3 * (doubles + triples)
            + 2.0 * homeRuns);
    }

    public int getWobaPlus() {
        return (int) (getWoba() * 100 / leagueBatting.getWoba());
    }

    public String getSlashLine() {
        DecimalFormat fmt = new DecimalFormat("#.000");
        return String.format(
            "%s/%s/%s",
            fmt.format(getAverage()),
            fmt.format(getOnBasePercentage()),
            fmt.format(getSluggingPercentage()));
    }

    private double perPlateAppearance(double value) {
        return getPlateAppearances() == 0
            ? 0
            : value / getPlateAppearances();
    }

    private double perPlateAppearance(int count) {
        return perPlateAppearance((double) count);
    }

    private double perHit(double value) {
        return getHits() == 0 ? 0 : value / getHits();
    }

    public BattingStats multiply(double d) {
        BattingStats stats = new BattingStats();
        stats.leagueBatting = leagueBatting;
        stats.atBats = (int) (atBats * d);
        stats.hits = (int) (hits * d);
        stats.doubles = (int) (doubles * d);
        stats.triples = (int) (triples * d);
        stats.homeRuns = (int) (homeRuns * d);
        stats.walks = (int) (walks * d);
        return stats;
    }

    public BattingStats add(BattingStats rhs) {
        BattingStats stats = new BattingStats();
        stats.leagueBatting = leagueBatting;
        stats.atBats = atBats + rhs.atBats;
        stats.hits = hits + rhs.hits;
        stats.doubles = doubles + rhs.doubles;
        stats.triples = triples + rhs.triples;
        stats.homeRuns = homeRuns + rhs.homeRuns;
        stats.walks = walks + rhs.walks;
        return stats;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}
