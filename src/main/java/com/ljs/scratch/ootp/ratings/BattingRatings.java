package com.ljs.scratch.ootp.ratings;

import javax.xml.bind.annotation.XmlElement;

/**
 *
 * @author lstephen
 */
public class BattingRatings implements BattingRatingsBuilder {

    public static enum BattingRatingsType { CONTACT, GAP, POWER, EYE }

    @XmlElement
    private int contact;

    @XmlElement
    private int gap;

    @XmlElement
    private int power;

    @XmlElement
    private int eye;

    public int getContact() {
        return contact;
    }

    public int getGap() {
        return gap;
    }

    public int getPower() {
        return power;
    }

    public int getEye() {
        return eye;
    }

    public BattingRatingsBuilder contact(Integer contact) {
        this.contact = contact;
        return this;
    }

    public BattingRatingsBuilder gap(Integer gap) {
        this.gap = gap;
        return this;
    }

    public BattingRatingsBuilder power(Integer power) {
        this.power = power;
        return this;
    }

    public BattingRatingsBuilder eye(Integer eye) {
        this.eye = eye;
        return this;
    }

    public BattingRatings build() {
        return this;
    }

}
