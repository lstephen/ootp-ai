package com.ljs.scratch.ootp.ratings;

import com.ljs.scratch.ootp.ratings.BattingRatings;

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
