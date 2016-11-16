package com.github.lstephen.ootp.ai.stats;

/** @author lstephen */
public final class SplitPercentagesHolder {

  private static SplitPercentages pcts;

  private SplitPercentagesHolder() {}

  public static SplitPercentages get() {
    return pcts;
  }

  public static void set(SplitPercentages pcts) {
    SplitPercentagesHolder.pcts = pcts;
  }
}
