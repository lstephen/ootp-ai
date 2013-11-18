/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ljs.ootp.ai.stats;

import com.google.common.base.Function;
import com.google.common.collect.Ordering;
import com.ljs.ootp.ai.player.Player;
import com.ljs.ootp.ai.player.ratings.PitchingRatings;
import com.ljs.ootp.ai.player.ratings.PlayerRatings;

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

        public Double getEraEstimate(PitchingStats stats) {
            return stats.getFip();
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

        public Double getEraEstimate(PitchingStats stats) {
            double wobaa = stats.getWobaAgainst();

            return Math.pow(wobaa / (1 - wobaa), 1.5) * 12;
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

    public abstract Double getEraEstimate(PitchingStats stats);

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