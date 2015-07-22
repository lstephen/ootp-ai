package com.ljs.ootp.extract.html.ootp6.rating;

import com.ljs.ootp.extract.html.rating.IntegerScale;
import com.ljs.ootp.extract.html.rating.Rating;

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

    public static Rating<Integer, OneToTen> valueOf(Integer value) {
        return Rating.create(value, INSTANCE);
    }

}
