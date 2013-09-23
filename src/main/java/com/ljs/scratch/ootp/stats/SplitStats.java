package com.ljs.scratch.ootp.stats;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ljs.scratch.ootp.ratings.Splits;

/**
 *
 * @author lstephen
 */
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

    @JsonCreator
    public static <S extends Stats<S>> SplitStats<S> create(
        @JsonProperty("vsLeft") S vsLeft,
        @JsonProperty("vsRight") S vsRight) {

        return new SplitStats<S>(vsLeft, vsRight);
    }

}
