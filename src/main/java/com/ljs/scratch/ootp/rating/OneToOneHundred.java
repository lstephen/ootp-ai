package com.ljs.scratch.ootp.rating;

/**
 *
 * @author lstephen
 */
public final class OneToOneHundred  implements Scale<Integer> {

    private static final OneToOneHundred INSTANCE = new OneToOneHundred();

    private OneToOneHundred() { }

    @Override
    public Rating<Integer, OneToOneHundred> normalize(Integer value) {
        return valueOf(value);
    }

    public static Rating<Integer, OneToOneHundred> valueOf(Integer value) {
        return Rating.create(value, INSTANCE);
    }

}
