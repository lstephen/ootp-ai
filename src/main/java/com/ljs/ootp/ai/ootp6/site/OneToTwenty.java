package com.ljs.ootp.ai.ootp6.site;

import com.ljs.ootp.ai.rating.IntegerScale;

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
