package com.github.lstephen.ootp.ai.stats;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.text.DecimalFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;

/** @author lstephen */
public class BattingStats implements Stats<BattingStats> {

  private Integer runs = 0;

  private int atBats;

  private int hits;

  private int doubles;

  private int triples;

  private int homeRuns;

  private int walks;

  private int ks;

  @JsonIgnore private BattingStats leagueBatting;

  public Integer getRuns() {
    return runs;
  }

  public void setRuns(Integer runs) {
    this.runs = runs;
  }

  public int getAtBats() {
    return atBats;
  }

  public void setAtBats(int atBats) {
    this.atBats = atBats;
  }

  public void setPlateAppearances(int pas) {
    setAtBats(pas - walks);
  }

  public int getHits() {
    return hits;
  }

  public void setHits(long hits) {
    this.hits = (int) hits;
  }

  public int getSingles() {
    return hits - doubles - triples - homeRuns;
  }

  public double getSinglesPerPlateAppearance() {
    return perPlateAppearance(getSingles());
  }

  public int getDoubles() {
    return doubles;
  }

  public void setDoubles(long doubles) {
    this.doubles = (int) doubles;
  }

  public int getTriples() {
    return triples;
  }

  public void setTriples(long triples) {
    this.triples = (int) triples;
  }

  public int getHomeRuns() {
    return homeRuns;
  }

  public void setHomeRuns(long homeRuns) {
    this.homeRuns = (int) homeRuns;
  }

  public int getWalks() {
    return walks;
  }

  public void setWalks(long walks) {
    this.walks = (int) walks;
  }

  public int getStrikeouts() {
    return ks;
  }

  public void setKs(long ks) {
    this.ks = (int) ks;
  }

  public void setLeagueBatting(BattingStats leagueBatting) {
    this.leagueBatting = leagueBatting;
  }

  public int getPlateAppearances() {
    return atBats + walks;
  }

  public double getHitsPerPlateAppearance() {
    return perPlateAppearance(hits);
  }

  public double getDoublesPerPlateAppearance() {
    return perPlateAppearance(doubles);
  }

  public double getTriplesPerPlateAppearance() {
    return perPlateAppearance(triples);
  }

  public double getExtraBaseHitsPerPlateAppearance() {
    return perPlateAppearance(doubles + triples);
  }

  public double getDoublesPerHit() {
    return perHit(doubles);
  }

  public double getTriplesPerHit() {
    return perHit(triples);
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

  public double getKsPerPlateAppearance() {
    return perPlateAppearance(ks);
  }

  public Integer getOuts() {
    return atBats - hits;
  }

  public double getOutsPerPlateAppearance() {
    return perPlateAppearance(getOuts());
  }

  public Integer getTotalBases() {
    return hits + doubles + 2 * triples + 3 * homeRuns;
  }

  public double getAverage() {
    return atBats == 0 ? 0 : (double) hits / atBats;
  }

  public double getOnBasePercentage() {
    return perPlateAppearance(walks + hits);
  }

  public double getSluggingPercentage() {
    return atBats == 0 ? 0 : (double) (hits + doubles + 2 * triples + 3 * homeRuns) / atBats;
  }

  public double getBabip() {
    if (atBats - ks - homeRuns == 0) {
      return 0;
    } else {
      return (double) (hits - homeRuns) / (atBats - ks - homeRuns);
    }
  }

  public double getWoba() {
    return Woba.get().calculate(this);
    /*return perPlateAppearance(
      0.7 * walks
    + 0.9 * (getSingles())
    + 1.3 * (doubles + triples)
    + 2.0 * homeRuns);*/
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
    return getPlateAppearances() == 0 ? 0 : value / getPlateAppearances();
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
    stats.runs = (int) (runs == null ? 0 : (runs * d));
    stats.atBats = (int) (atBats * d);
    stats.hits = (int) (hits * d);
    stats.doubles = (int) (doubles * d);
    stats.triples = (int) (triples * d);
    stats.homeRuns = (int) (homeRuns * d);
    stats.walks = (int) (walks * d);
    stats.ks = (int) (ks * d);
    return stats;
  }

  public BattingStats add(BattingStats rhs) {
    BattingStats stats = new BattingStats();
    stats.leagueBatting = leagueBatting;
    stats.runs = (runs == null ? 0 : runs) + (rhs.runs == null ? 0 : rhs.runs);
    stats.atBats = atBats + rhs.atBats;
    stats.hits = hits + rhs.hits;
    stats.doubles = doubles + rhs.doubles;
    stats.triples = triples + rhs.triples;
    stats.homeRuns = homeRuns + rhs.homeRuns;
    stats.walks = walks + rhs.walks;
    stats.ks = ks + rhs.ks;
    return stats;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }
}
