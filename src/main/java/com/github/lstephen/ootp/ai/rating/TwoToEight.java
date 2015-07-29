package com.github.lstephen.ootp.ai.rating;

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
