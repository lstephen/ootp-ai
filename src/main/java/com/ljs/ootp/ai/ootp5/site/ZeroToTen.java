package com.ljs.ootp.ai.ootp5.site;

import com.ljs.ootp.ai.rating.IntegerScale;

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
