package com.ljs.scratch.ootp.value;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.ljs.scratch.ootp.core.Player;
import com.ljs.scratch.ootp.regression.Predictions;
import com.ljs.scratch.ootp.selection.HitterSelectionFactory;
import com.ljs.scratch.ootp.selection.Mode;
import com.ljs.scratch.ootp.selection.PitcherSelectionFactory;
import com.ljs.scratch.ootp.selection.Selection;
import com.ljs.scratch.ootp.selection.Selections;
import com.ljs.scratch.ootp.selection.Slot;
import java.io.PrintWriter;
import java.util.List;

/**
 *
 * @author lstephen
 */
public class ReplacementValue {

    private final Predictions predictions;

    private final Function<Player, Integer> value;

    private final Function<Player, Integer> selectionValue;

    private final Selection hitterSelection;

    private final Selection pitcherSelection;

    public ReplacementValue(Predictions ps, Function<Player, Integer> value, Function<Player, Integer> selectionValue) {
        this.predictions = ps;
        this.value = value;
        this.selectionValue = selectionValue;

        hitterSelection = HitterSelectionFactory
            .using(selectionValue)
            .create(Mode.REGULAR_SEASON);

        pitcherSelection = PitcherSelectionFactory
            .using(selectionValue, predictions.getPitcherOverall())
            .create(Mode.REGULAR_SEASON);
    }

    public Integer getValueVsReplacement(Player p) {
        List<Integer> values = Lists.newArrayList();

        for (Slot s : p.getSlots()) {
            values.add(value.apply(p) - getReplacementLevel(s));
        }

        return Ordering.natural().max(values);
    }

    public void print(PrintWriter w) {
        for (Slot s : Slot.values()) {
            if (s == Slot.P) {
                continue;
            }
            w.print(String.format(" %s:%3d ", s, getReplacementLevel(s)));
        }
        w.println();
    }

    private Integer getReplacementLevel(final Slot s) {
        Iterable<Player> slotSelections;

        switch (s) {
            case C:
            case SS:
            case CF:
            case IF:
            case OF:
            case H:
                slotSelections = Selections
                    .select(
                        hitterSelection,
                        Selections.onlyHitters(predictions.getAllPlayers()))
                    .get(s);

                break;
            case SP:
            case MR:
                ImmutableMultimap<Slot, Player> pitchingSelection = Selections
                    .select(
                        pitcherSelection,
                        Selections.onlyPitchers(predictions.getAllPlayers()));

                switch (s) {
                    case SP:
                        slotSelections = pitchingSelection.get(Slot.SP);
                        break;

                    case MR:
                        slotSelections = Iterables.concat(
                            pitchingSelection.get(Slot.MR),
                            pitchingSelection.get(Slot.P));
                        break;
                    default:
                        throw new IllegalStateException();
                }

                break;
            case P:
                return Integer.MAX_VALUE;
            default:
                throw new IllegalStateException();
        }

        if (Iterables.isEmpty(slotSelections)) {
            return 0;
        }

        return Ordering
            .natural()
            .min(Iterables.transform(
                slotSelections,
                new Function<Player, Integer>() {
                    @Override
                    public Integer apply(Player p) {
                        if (s == Slot.MR && Slot.getPrimarySlot(p) == Slot.SP) {
                            return (int) (PlayerValue.MR_CONSTANT * value.apply(p));
                        }
                        return value.apply(p);
                    }
                }));
    }

}
