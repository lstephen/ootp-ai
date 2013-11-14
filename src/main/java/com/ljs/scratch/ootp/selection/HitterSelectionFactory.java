package com.ljs.scratch.ootp.selection;

import com.ljs.scratch.ootp.player.Slot;
import com.google.common.base.Function;
import com.google.common.collect.Ordering;
import com.ljs.scratch.ootp.player.Player;
import com.ljs.scratch.ootp.regression.Predictions;
import com.ljs.scratch.ootp.stats.BattingStats;
import com.ljs.scratch.ootp.stats.TeamStats;
import org.fest.assertions.api.Assertions;

public final class HitterSelectionFactory implements SelectionFactory {

    private final Function<Player, Double> value;

    private HitterSelectionFactory(Function<Player, Double> value) {
        this.value = value;
    }

    public Selection create(Mode mode) {
        return SlotSelection
            .builder()
            .ordering(byOverall())
            .slots(mode.getHittingSlots())
            .size(mode.getHittingSlots().size())
            .fillToSize(Slot.H)
            .build();
    }

    public Ordering<Player> byOverall() {
        return Ordering.natural().reverse().onResultOf(value).compound(Player
            .byWeightedBattingRating()).compound(Player.byAge());
    }

    public static HitterSelectionFactory using(Predictions predictions) {
        return using(predictions.getAllBatting());
    }

    public static HitterSelectionFactory using(
        TeamStats<BattingStats> stats) {

        return using(defaultValueFunction(stats));
    }

    public static HitterSelectionFactory using(
        final Function<Player, ? extends Number> value) {

        return new HitterSelectionFactory(new Function<Player, Double>() {
            @Override
            public Double apply(Player p) {
                return Double.valueOf(((Number) value.apply(p)).doubleValue());
            }
        });
    }

    private static Function<Player, Double> defaultValueFunction(
        final TeamStats<BattingStats> batting) {

        return new Function<Player, Double>() {
            @Override
            public Double apply(Player p) {
                Assertions.assertThat(p).isNotNull();
                return batting.getOverall(p).getWoba();
            }
        };
    }

}
