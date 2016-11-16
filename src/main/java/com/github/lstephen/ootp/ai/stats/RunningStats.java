package com.github.lstephen.ootp.ai.stats;

public class RunningStats {

  private int stolenBases;

  private int caughtStealing;

  public int getStolenBases() {
    return stolenBases;
  }

  public void setStolenBases(int stolenBases) {
    this.stolenBases = stolenBases;
  }

  public int getCaughtStealing() {
    return caughtStealing;
  }

  public void setCaughtStealing(int caughtStealing) {
    this.caughtStealing = caughtStealing;
  }
}
