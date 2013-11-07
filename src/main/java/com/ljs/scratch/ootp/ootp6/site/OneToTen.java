package com.ljs.scratch.ootp.ootp6.site;

import com.ljs.scratch.ootp.rating.IntegerScale;

/**
 *
 * @author lstephen
 */
public final class OneToTen extends IntegerScale {

    private static final OneToTen INSTANCE = new OneToTen();

    private OneToTen() {
        super();
    }

    @Override
    protected Integer scale(Integer value) {
        return value * 10 - 5;
    }

    public static OneToTen scale() {
        return INSTANCE;
    }

}
