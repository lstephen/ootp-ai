package com.ljs.ootp.extract.html.ootp6.rating;

import com.ljs.ootp.extract.html.rating.IntegerScale;

/**
 *
 * @author lstephen
 */
public final class TwoToEight extends IntegerScale {

    private static final TwoToEight INSTANCE = new TwoToEight();

    private TwoToEight() {
        super();
    }

    @Override
    protected Integer scale(Integer value) {
        return (value * 2 + (value - 5)) * 5;
    }

    public static TwoToEight scale() {
        return INSTANCE;
    }

}
