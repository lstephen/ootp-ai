/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.lstephen.ootp.ai.stats;

import com.google.common.base.Function;
import com.google.common.collect.Ordering;
import com.github.lstephen.ootp.ai.player.Player;
import com.github.lstephen.ootp.ai.player.ratings.PitchingRatings;
import com.github.lstephen.ootp.ai.player.ratings.PlayerRatings;

/**
 *
 * @author lstephen
 * @deprecated Both FIP and WOBA against are the same now
 */
public enum PitcherOverall {
    FIP {
        public Double get(
            TeamStats<PitchingStats> predictions, Player p) {
          return WOBA_AGAINST.get(predictions, p);
        }

        public Integer getPlus(
            TeamStats<PitchingStats> predictions, Player p) {
          return WOBA_AGAINST.getPlus(predictions, p);
        }

        public Integer getPlus(PitchingStats stats) {
          return WOBA_AGAINST.getPlus(stats);
        }

        public Double getEraEstimate(PitchingStats stats) {
          return WOBA_AGAINST.getEraEstimate(stats);
        }
    }, WOBA_AGAINST {
        public Double get(
            TeamStats<PitchingStats> predictions, Player p) {
            return getEraEstimate(predictions.getOverall(p));
        }

        public Integer getPlus(
            TeamStats<PitchingStats> predictions, Player p) {
            return getPlus(predictions.getOverall(p));
        }

        public Integer getPlus(PitchingStats stats) {
            return stats.getBaseRunsPlus(EraBaseRuns.get());
        }

        public Double getEraEstimate(PitchingStats stats) {
            return stats.getBaseRuns(EraBaseRuns.get());
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
