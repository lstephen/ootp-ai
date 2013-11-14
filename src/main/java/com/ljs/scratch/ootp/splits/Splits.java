package com.ljs.scratch.ootp.splits;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 *
 * @author lstephen
 */
public class Splits<T> {

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
    private final T vsLeft;

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
    private final T vsRight;

    @JsonCreator
    protected Splits(
        @JsonProperty("vsLeft") T vsLeft,
        @JsonProperty("vsRight") T vsRight) {

        this.vsLeft = vsLeft;
        this.vsRight = vsRight;
    }

    public T getVsLeft() {
        return vsLeft;
    }

    public T getVsRight() {
        return vsRight;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    public static <T> Splits<T> create(T vsLeft, T vsRight) {
        return new Splits<T>(vsLeft, vsRight);
    }

}
