package com.ljs.ootp.extract.html.ootp6.rating;

import com.ljs.ootp.extract.html.rating.IntegerScale;

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
