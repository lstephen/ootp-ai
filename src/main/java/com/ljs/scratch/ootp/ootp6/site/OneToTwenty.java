package com.ljs.scratch.ootp.ootp6.site;

import com.ljs.scratch.ootp.rating.IntegerScale;

/**
 *
 * @author lstephen
 */
public final class OneToTwenty extends IntegerScale {

    private static final OneToTwenty INSTANCE = new OneToTwenty();

    private OneToTwenty() {
        super();
    }

    @Override
    protected Integer scale(Integer value) {
        return value * 5;
    }

    public static OneToTwenty scale() {
        return INSTANCE;
    }

}
