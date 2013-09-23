package com.ljs.scratch.ootp.ratings;

import com.ljs.scratch.ootp.ratings.BattingRatings;

/**
 *
 * @author lstephen
 */
public interface BattingRatingsBuilder {

        BattingRatingsBuilder contact(Integer contact);
        BattingRatingsBuilder gap(Integer gap);
        BattingRatingsBuilder power(Integer power);
        BattingRatingsBuilder eye(Integer eye);

        BattingRatings build();

}
