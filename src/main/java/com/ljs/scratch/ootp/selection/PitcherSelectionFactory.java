package com.ljs.scratch.ootp.selection;

import com.google.common.base.Function;
import com.google.common.collect.Ordering;
import com.ljs.scratch.ootp.core.Player;
import com.ljs.scratch.ootp.regression.Predictions;
import com.ljs.scratch.ootp.stats.PitcherOverall;
import com.ljs.scratch.ootp.stats.PitchingStats;
import com.ljs.scratch.ootp.stats.TeamStats;

/**
 *
 * @author lstephen
 */
public final class PitcherSelectionFactory {

    private final Function<Player, Double> value;

    private PitcherOverall overall;

    private PitcherSelectionFactory(Function<Player, Double> value, PitcherOverall overall) {
        this.value = value;
        this.overall = overall;
    }

    public Selection create(Mode mode) {
        return SlotSelection
            .builder()
            .ordering(byOverall())
            .slots(mode.getPitchingSlots())
            .size(mode.getPitchingSlots().size())
            .build();
    }

    public Ordering<Player> byOverall() {
        return Ordering
            .natural()
            .reverse()
            .onResultOf(value)
            .compound(overall.byWeightedRating())
            .compound(Player.byAge());
    }

    public static PitcherSelectionFactory using(Predictions predictions) {
        return using(
            predictions.getAllPitching(),
            predictions.getPitcherOverall());
    }

    public static PitcherSelectionFactory using(
        TeamStats<PitchingStats> stats, PitcherOverall overall) {

        return using(defaultValueFunction(stats, overall), overall);
    }

    public static PitcherSelectionFactory using(
        final Function<Player, ? extends Number> value,
        PitcherOverall overall) {

        return new PitcherSelectionFactory(
            new Function<Player, Double>() {
                @Override
                public Double apply(Player input) {
                    return value.apply(input).doubleValue();
                }},
            overall);
    }

    private static Function<Player, Integer> defaultValueFunction(
        final TeamStats<PitchingStats> pitching, final PitcherOverall overall) {

        return new Function<Player, Integer>() {
            public Integer apply(Player p) {
                return overall.getPlus(pitching, p);
            }
        };
    }





}
