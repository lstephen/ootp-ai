package com.github.lstephen.ootp.ai.rating;

/**
 *
 * @author lstephen
 */
public final class ZeroToTen extends IntegerScale {

    private static final ZeroToTen INSTANCE = new ZeroToTen();

    private ZeroToTen() {
        super();
    }

    @Override
    protected Integer scale(Integer value) {
        return value == 0 ? 1 : value * 10;
    }

    public static ZeroToTen scale() {
        return INSTANCE;
    }

}
