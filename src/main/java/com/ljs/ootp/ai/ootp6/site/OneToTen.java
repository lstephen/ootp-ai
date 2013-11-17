package com.ljs.ootp.ai.ootp6.site;

import com.ljs.ootp.ai.rating.IntegerScale;

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
