package com.github.lstephen.ootp.ai.value;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.github.lstephen.ootp.ai.player.Player;
import com.github.lstephen.ootp.ai.player.Slot;
import com.github.lstephen.ootp.ai.regression.Predictions;
import com.github.lstephen.ootp.ai.selection.HitterSelectionFactory;
import com.github.lstephen.ootp.ai.selection.Mode;
import com.github.lstephen.ootp.ai.selection.PitcherSelectionFactory;
import com.github.lstephen.ootp.ai.selection.Selection;
import com.github.lstephen.ootp.ai.selection.Selections;
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
            .using(ps, selectionValue)
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
            if (Slot.getPrimarySlot(p) == Slot.MR && s == Slot.P) {
                values.add((int) (value.apply(p) / PlayerValue.MR_CONSTANT - getReplacementLevel(s)));
            } else {
                values.add(value.apply(p) - getReplacementLevel(s));
            }
        }

        return Ordering.natural().max(values);
    }

    public void print(PrintWriter w) {
        for (Slot s : Slot.values()) {
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
            case P:
                slotSelections = pitcherSlotSelections.get(s);
                break;
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
                        } else if (s == Slot.P && Slot.getPrimarySlot(p) == Slot.MR) {
                            return (int) (value.apply(p) / PlayerValue.MR_CONSTANT);
                        }
                        return value.apply(p);
                    }
                }));
    }

}
