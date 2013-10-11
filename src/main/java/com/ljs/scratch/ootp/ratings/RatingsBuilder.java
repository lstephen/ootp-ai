package com.ljs.scratch.ootp.ratings;

/**
 *
 * @author lstephen
 */
public final class RatingsBuilder {

    private RatingsBuilder() { }

    public static BattingRatingsBuilder batting() {
        return new BattingRatings();
    }

}
