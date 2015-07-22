package com.ljs.ootp.extract.html.rating;

/**
 *
 * @author lstephen
 */
public final class OneToOneHundred extends IntegerScale {

    private static final OneToOneHundred INSTANCE = new OneToOneHundred();

    private OneToOneHundred() {
        super();
    }

    @Override
    protected Integer scale(Integer value) {
        return value;
    }

    public static Rating<Integer, OneToOneHundred> valueOf(Integer value) {
        return Rating.create(value, INSTANCE);
    }

    public static OneToOneHundred scale() {
        return INSTANCE;
    }

}
