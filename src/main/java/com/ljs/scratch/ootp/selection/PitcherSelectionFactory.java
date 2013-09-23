package com.ljs.scratch.ootp.selection;

import com.google.common.base.Function;
import com.google.common.collect.Ordering;
import com.ljs.scratch.ootp.core.Player;
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
            .onResultOf(value)
            .compound(overall.byWeightedRating())
            .compound(Player.byAge());
    }

    public static PitcherSelectionFactory using(
        TeamStats<PitchingStats> stats, PitcherOverall overall) {

        return using(defaultValueFunction(stats, overall), overall);
    }

    public static PitcherSelectionFactory using(
        Function<Player, Double> value, PitcherOverall overall) {

        return new PitcherSelectionFactory(value, overall);
    }

    private static Function<Player, Double> defaultValueFunction(
        final TeamStats<PitchingStats> pitching, final PitcherOverall overall) {

        return new Function<Player, Double>() {
            public Double apply(Player p) {
                return overall.get(pitching, p);
            }
        };
    }





}
