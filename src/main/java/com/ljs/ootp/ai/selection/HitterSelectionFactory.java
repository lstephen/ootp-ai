package com.ljs.ootp.ai.selection;

import com.google.common.base.Function;
import com.google.common.collect.Ordering;
import com.ljs.ootp.ai.player.Player;
import com.ljs.ootp.ai.player.Slot;
import com.ljs.ootp.ai.regression.Predictions;
import com.ljs.ootp.ai.stats.BattingStats;
import com.ljs.ootp.ai.stats.TeamStats;
import org.fest.assertions.api.Assertions;

public final class HitterSelectionFactory implements SelectionFactory {

    private final Function<Player, Integer> value;

    private HitterSelectionFactory(Function<Player, Integer> value) {
        this.value = value;
    }

    @Override
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
        return Ordering
            .natural()
            .reverse()
            .onResultOf(value)
            .compound(Player.byTieBreak());
    }

    public static HitterSelectionFactory using(Predictions predictions) {
        return using(predictions.getAllBatting());
    }

    public static HitterSelectionFactory using(
        TeamStats<BattingStats> stats) {

        return using(defaultValueFunction(stats));
    }

    public static HitterSelectionFactory using(
        final Function<Player, Integer> value) {
        return new HitterSelectionFactory(value);
    }

    private static Function<Player, Integer> defaultValueFunction(
        final TeamStats<BattingStats> batting) {

        return new Function<Player, Integer>() {
            @Override
            public Integer apply(Player p) {
                Assertions.assertThat(p).isNotNull();
                return batting.getOverall(p).getWobaPlus();
            }
        };
    }

}
