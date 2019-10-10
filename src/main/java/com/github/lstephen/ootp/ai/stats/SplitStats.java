package com.github.lstephen.ootp.ai.stats;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.lstephen.ootp.ai.splits.Splits;

/** @author lstephen */
public class SplitStats<S extends Stats<S>> extends Splits<S> {

  private static SplitPercentages percentages;

  protected SplitStats(S vsLeft, S vsRight) {
    super(vsLeft, vsRight);
  }

  public S getOverall() {
    return percentages.combine(getVsLeft(), getVsRight());
  }

  public static void setPercentages(SplitPercentages percentages) {
    SplitStats.percentages = percentages;
  }

  public SplitStats<S> add(SplitStats<S> rhs) {
    return new SplitStats(getVsLeft().add(rhs.getVsLeft()), getVsRight().add(rhs.getVsRight()));
  }

  public SplitStats<S> multiply(double factor) {
    return new SplitStats(getVsLeft().multiply(factor), getVsRight().multiply(factor));
  }

  @JsonCreator
  public static <S extends Stats<S>> SplitStats<S> create(
      @JsonProperty("vsLeft") S vsLeft, @JsonProperty("vsRight") S vsRight) {

    return new SplitStats<S>(vsLeft, vsRight);
  }
}
