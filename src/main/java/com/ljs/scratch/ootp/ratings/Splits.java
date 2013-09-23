package com.ljs.scratch.ootp.ratings;

import javax.xml.bind.annotation.XmlElement;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 *
 * @author lstephen
 */
public class Splits<T> {

    @XmlElement
    private T vsLeft;

    @XmlElement
    private T vsRight;

    protected Splits() { /* JAXB */ }

    protected Splits(T vsLeft, T vsRight) {
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
