/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ljs.scratch.ootp.stats;

import com.google.common.base.Function;
import com.google.common.collect.Ordering;
import com.ljs.scratch.ootp.core.Player;
import com.ljs.scratch.ootp.ratings.PitchingRatings;
import com.ljs.scratch.ootp.ratings.PlayerRatings;

/**
 *
 * @author lstephen
 */
public enum PitcherOverall {
    FIP {
        public Double get(
            TeamStats<PitchingStats> predictions, Player p) {
            return predictions.getOverall(p).getFip();
        }

        public Integer getPlus(
            TeamStats<PitchingStats> predictions, Player p) {
            return predictions.getOverall(p).getFipPlus();
        }

        public Integer getPlus(PitchingStats stats) {
            return stats.getFipPlus();
        }
    }, WOBA_AGAINST {
        public Double get(
            TeamStats<PitchingStats> predictions, Player p) {
            return predictions.getOverall(p).getWobaAgainst();
        }

        public Integer getPlus(
            TeamStats<PitchingStats> predictions, Player p) {
            return predictions.getOverall(p).getWobaPlusAgainst();
        }

        public Integer getPlus(PitchingStats stats) {
            return stats.getWobaPlusAgainst();
        }
    };

    public abstract Double get(
        TeamStats<PitchingStats> predictions, Player p);

    public abstract Integer getPlus(
        TeamStats<PitchingStats> predictions, Player p);

    public Integer getPlus(SplitStats<PitchingStats> stats) {
        return getPlus(stats.getOverall());
    }

    public abstract Integer getPlus(PitchingStats stats);

    public Ordering<Player> byWeightedRating() {
        return Ordering
            .natural()
            .reverse()
            .onResultOf(new Function<Player, Double>() {
                public Double apply(Player p) {
                    PitchingRatings ratings =
                        PlayerRatings.getOverallPitching(p.getPitchingRatings());

                    switch (PitcherOverall.this) {
                        case FIP:
                            return Double.valueOf(
                                2 * ratings.getStuff()
                                    + 3 * ratings.getControl()
                                    + 13 * ratings.getMovement());
                        case WOBA_AGAINST:
                            return 0.7 * ratings.getControl()
                                + 0.9 * ratings.getHits()
                                + 1.3 * ratings.getGap()
                                + 2.0 * ratings.getMovement();
                        default:
                            throw new IllegalStateException();
                    }
                }
            });

    }


}
