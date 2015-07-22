package com.ljs.ootp.extract.html.ootp5.rating;

import com.ljs.ootp.extract.html.rating.IntegerScale;

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
        return value * 10;
    }

    public static ZeroToTen scale() {
        return INSTANCE;
    }

}
