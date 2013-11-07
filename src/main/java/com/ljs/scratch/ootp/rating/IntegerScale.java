package com.ljs.scratch.ootp.rating;

/**
 *
 * @author lstephen
 */
public abstract class IntegerScale implements Scale<Integer> {

    @Override
    public Rating<Integer, IntegerScale> ratingOf(Integer value) {
        return Rating.create(value, this);
    }

    @Override
    public Rating<Integer, IntegerScale> parse(String s) {
        return Rating.create(Integer.parseInt(s), this);
    }

    @Override
    public Rating<Integer, OneToOneHundred> normalize(Integer value) {
        return OneToOneHundred.valueOf(scale(value));
    }

    protected abstract Integer scale(Integer value);

}
