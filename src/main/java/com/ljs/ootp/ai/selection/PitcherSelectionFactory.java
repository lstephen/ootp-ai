package com.ljs.ootp.ai.selection;

import com.google.common.base.Function;
import com.google.common.base.Throwables;
import com.google.common.collect.Ordering;
import com.ljs.ootp.ai.player.Player;
import com.ljs.ootp.ai.player.Slot;
import com.ljs.ootp.ai.regression.Predictions;
import com.ljs.ootp.ai.stats.PitcherOverall;
import com.ljs.ootp.ai.stats.PitchingStats;
import com.ljs.ootp.ai.stats.TeamStats;

/**
 *
 * @author lstephen
 */
public final class PitcherSelectionFactory implements SelectionFactory {

    private final Function<Player, Integer> value;

    private PitcherSelectionFactory(
        Function<Player, Integer> value) {

        this.value = value;
    }

    @Override
    public Selection create(Mode mode) {
        return SlotSelection
            .builder()
            .ordering(byOverall())
            .slots(mode.getPitchingSlots())
            .size(mode.getPitchingSlots().size())
            .fillToSize(Slot.P)
            .build();
    }

    public Ordering<Player> byOverall() {
        return Ordering
            .natural()
            .reverse()
            .onResultOf(value)
            .compound(Player.byTieBreak());
    }

    public static PitcherSelectionFactory using(Predictions predictions) {
        return using(
            predictions.getAllPitching(),
            predictions.getPitcherOverall());
    }

    public static PitcherSelectionFactory using(
        TeamStats<PitchingStats> stats, PitcherOverall overall) {

        return using(defaultValueFunction(stats, overall));
    }

    public static PitcherSelectionFactory using(
        final Function<Player, Integer> value) {

        return new PitcherSelectionFactory(value);
    }

    private static Function<Player, Integer> defaultValueFunction(
        final TeamStats<PitchingStats> pitching, final PitcherOverall overall) {

        return new Function<Player, Integer>() {
            public Integer apply(Player p) {
                try {
                    return overall.getPlus(pitching, p);
                } catch (Exception e) {
                    throw Throwables.propagate(e);
                }
            }
        };
    }





}
