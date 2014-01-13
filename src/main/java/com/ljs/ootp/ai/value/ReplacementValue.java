package com.ljs.ootp.ai.value;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.ljs.ootp.ai.player.Player;
import com.ljs.ootp.ai.player.Slot;
import com.ljs.ootp.ai.regression.Predictions;
import com.ljs.ootp.ai.selection.HitterSelectionFactory;
import com.ljs.ootp.ai.selection.Mode;
import com.ljs.ootp.ai.selection.PitcherSelectionFactory;
import com.ljs.ootp.ai.selection.Selection;
import com.ljs.ootp.ai.selection.Selections;
import java.io.PrintWriter;
import java.util.List;

/**
 *
 * @author lstephen
 */
public class ReplacementValue {

    private final Function<Player, Integer> value;

    private final ImmutableMultimap<Slot, Player> hitterSlotSelections;

    private final ImmutableMultimap<Slot, Player> pitcherSlotSelections;

    public ReplacementValue(Predictions ps, Function<Player, Integer> value, Function<Player, Integer> selectionValue) {
        this.value = value;

        Selection hitterSelection = HitterSelectionFactory
            .using(ps.getAllBatting(), selectionValue)
            .slot(Mode.REGULAR_SEASON);

        Selection pitcherSelection = PitcherSelectionFactory
            .using(selectionValue)
            .slot(Mode.REGULAR_SEASON);

        hitterSlotSelections = Selections
            .select(
                hitterSelection,
                Selections.onlyHitters(ps.getAllPlayers()));

        pitcherSlotSelections = Selections
            .select(
                pitcherSelection,
                Selections.onlyPitchers(ps.getAllPlayers()));

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
                slotSelections = hitterSlotSelections.get(s);
                break;
            case SP:
            case MR:
                switch (s) {
                    case SP:
                        slotSelections = pitcherSlotSelections.get(Slot.SP);
                        break;

                    case MR:
                        slotSelections = Iterables.concat(
                            pitcherSlotSelections.get(Slot.MR),
                            pitcherSlotSelections.get(Slot.P));
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
